package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.watabou.noosa.Game;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Undo and Redo. Each time your hero is ready for a new action the game is saved and copies of the game file and
 * the current floor's file are kept in memory. Undo and Redo write a copy back and reload the game from it.
 */
@SuppressWarnings("unused")
public final class UndoHistory {

	private static final String TAG = "UndoHistory";

	static final int MODE_LAST = 0;
	static final int MODE_FLOOR = 1;
	static final int MODE_UNLIMITED = 2;
	static final String[] MODES = { "Last actions", "This floor", "Unlimited" };
	static final int DEFAULT_SIZE = 20;
	static final int MAX_SIZE = 200;

	private static final String KEY_ON = "qol_undo_on";
	private static final String KEY_MODE = "qol_undo_mode";
	private static final String KEY_SIZE = "qol_undo_size";
	private static final String KEY_FLOOR_ONLY = "qol_undo_floor_only";
	private static final String KEY_NO_BOSS = "qol_undo_no_boss";

	private static final class Snapshot {
		final byte[] game;
		final String levelFile;
		final byte[] level;
		final int depth;
		final int branch;

		Snapshot(byte[] game, String levelFile, byte[] level, int depth, int branch) {
			this.game = game;
			this.levelFile = levelFile;
			this.level = level;
			this.depth = depth;
			this.branch = branch;
		}

		int size() {
			return game.length + level.length;
		}
	}

	private static final List<Snapshot> history = new ArrayList<>();
	private static int index = -1;
	private static String gameId;
	private static boolean skipNext;
	private static boolean pending;

	private static long lastSaveNanos;
	private static long lastCopyNanos;
	private static long slowestNanos;
	private static long totalNanos;
	private static int timedSnapshots;

	private UndoHistory() {
	}

	static boolean isOn() {
		return QolSettings.isAvailable() && CloverlordSettings.get(KEY_ON);
	}

	static void setOn(boolean on) {
		CloverlordSettings.set(KEY_ON, on);
		if (!on) clear();
	}

	static int mode() {
		int mode = CloverlordSettings.getInt(KEY_MODE);
		return mode < MODE_LAST || mode > MODE_UNLIMITED ? MODE_LAST : mode;
	}

	static void mode(int mode) {
		CloverlordSettings.setInt(KEY_MODE, mode);
		trim();
	}

	static int size() {
		int size = CloverlordSettings.getInt(KEY_SIZE);
		return size <= 0 ? DEFAULT_SIZE : Math.min(MAX_SIZE, size);
	}

	static void size(int size) {
		CloverlordSettings.setInt(KEY_SIZE, Math.max(1, Math.min(MAX_SIZE, size)));
		trim();
	}

	static boolean acrossFloors() {
		return !CloverlordSettings.get(KEY_FLOOR_ONLY);
	}

	static void acrossFloors(boolean on) {
		CloverlordSettings.set(KEY_FLOOR_ONLY, !on);
	}

	static boolean duringBosses() {
		return !CloverlordSettings.get(KEY_NO_BOSS);
	}

	static void duringBosses(boolean on) {
		CloverlordSettings.set(KEY_NO_BOSS, !on);
	}

	/**
	 * Called at the end of Hero.ready, when the hero waits for your next action. The snapshot is taken on the
	 * render thread, while the game waits for input.
	 */
	public static void onHeroReady() {
		if (!isOn() || pending) return;
		pending = true;
		Game.runOnRenderThread(() -> {
			pending = false;
			capture();
		});
	}

	private static void capture() {
		Hero hero = Dungeon.hero;
		if (!isOn() || hero == null || !hero.isAlive() || !hero.ready) return;
		String id = GamesInProgress.curSlot + ":" + Dungeon.seed;
		if (!id.equals(gameId)) {
			clear();
			gameId = id;
		}
		if (skipNext) {
			skipNext = false;
			return;
		}
		try {
			long start = System.nanoTime();
			Dungeon.saveAll();
			long saved = System.nanoTime();
			File root = SaveLocation.currentRoot();
			if (root == null) return;
			String levelFile = GamesInProgress.depthFile(GamesInProgress.curSlot, Dungeon.depth, Dungeon.branch);
			byte[] game = read(new File(root, GamesInProgress.gameFile(GamesInProgress.curSlot)));
			byte[] level = read(new File(root, levelFile));
			long copied = System.nanoTime();
			push(new Snapshot(game, levelFile, level, Dungeon.depth, Dungeon.branch));
			recordTiming(saved - start, copied - saved);
		} catch (Throwable e) {
			Log.e(TAG, "Could not take a snapshot", e);
		}
	}

	private static void push(Snapshot snapshot) {
		while (history.size() > index + 1) history.remove(history.size() - 1);
		Snapshot last = index >= 0 ? history.get(index) : null;
		boolean newFloor = last != null && (last.depth != snapshot.depth || last.branch != snapshot.branch);
		if (newFloor && (mode() == MODE_FLOOR || !acrossFloors())) history.clear();
		history.add(snapshot);
		index = history.size() - 1;
		trim();
	}

	private static void trim() {
		if (mode() != MODE_LAST) return;
		while (history.size() > size()) {
			history.remove(0);
			index--;
		}
		if (index < -1) index = -1;
	}

	private static boolean allowedNow() {
		Hero hero = Dungeon.hero;
		if (!isOn() || hero == null || !hero.isAlive() || !hero.ready) return false;
		return duringBosses() || Dungeon.level == null || !Dungeon.level.locked;
	}

	static boolean canUndo() {
		return index > 0 && allowedNow();
	}

	static boolean canRedo() {
		return index >= 0 && index < history.size() - 1 && allowedNow();
	}

	static void undo() {
		if (canUndo()) restore(index - 1);
	}

	static void redo() {
		if (canRedo()) restore(index + 1);
	}

	private static void restore(int target) {
		Snapshot snapshot = history.get(target);
		try {
			File root = SaveLocation.currentRoot();
			if (root == null) return;
			write(new File(root, GamesInProgress.gameFile(GamesInProgress.curSlot)), snapshot.game);
			write(new File(root, snapshot.levelFile), snapshot.level);
			index = target;
			skipNext = true;
			GamesInProgress.setUnknown(GamesInProgress.curSlot);
			InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
			Game.switchScene(InterlevelScene.class);
		} catch (Throwable e) {
			Log.e(TAG, "Could not restore a snapshot", e);
		}
	}

	static void clear() {
		history.clear();
		index = -1;
		skipNext = false;
	}

	private static void recordTiming(long saveNanos, long copyNanos) {
		lastSaveNanos = saveNanos;
		lastCopyNanos = copyNanos;
		long total = saveNanos + copyNanos;
		slowestNanos = Math.max(slowestNanos, total);
		totalNanos += total;
		timedSnapshots++;
	}

	static void resetTiming() {
		lastSaveNanos = lastCopyNanos = slowestNanos = totalNanos = 0;
		timedSnapshots = 0;
	}

	static String lastTiming() {
		if (timedSnapshots == 0) return "None yet";
		return ms(lastSaveNanos + lastCopyNanos) + " (save " + ms(lastSaveNanos) + ", copy " + ms(lastCopyNanos) + ")";
	}

	static String averageTiming() {
		return timedSnapshots == 0 ? "None yet" : ms(totalNanos / timedSnapshots) + " over " + timedSnapshots;
	}

	static String slowestTiming() {
		return timedSnapshots == 0 ? "None yet" : ms(slowestNanos);
	}

	static String memoryText() {
		long bytes = 0;
		for (Snapshot snapshot : history) bytes += snapshot.size();
		return history.size() + " kept, " + Math.round(bytes / 1024f) + " KB";
	}

	private static String ms(long nanos) {
		return Math.round(nanos / 100_000f) / 10f + " ms";
	}

	private static byte[] read(File file) throws IOException {
		try (InputStream in = new FileInputStream(file)) {
			byte[] data = new byte[(int) file.length()];
			int offset = 0;
			while (offset < data.length) {
				int read = in.read(data, offset, data.length - offset);
				if (read < 0) break;
				offset += read;
			}
			return data;
		}
	}

	private static void write(File file, byte[] data) throws IOException {
		try (OutputStream out = new FileOutputStream(file)) {
			out.write(data);
		}
	}
}
