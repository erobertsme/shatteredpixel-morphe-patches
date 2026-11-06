package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.utils.Bundle;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Edits enemies, NPCs and other characters, and keeps alignment changes in their save data.
 */
@SuppressWarnings("unused")
public final class EnemyEditor {

	private static final String TAG = "EnemyEditor";
	private static final String KEY_ALIGNMENT = "morphe_extras_alignment";

	private static final Set<Char> editedAlignment = Collections.newSetFromMap(new WeakHashMap<>());
	private static final ArrayDeque<Object[]> restoring = new ArrayDeque<>();
	private static Object infoMob;

	private EnemyEditor() {
	}

	static void setAlignment(Mob mob, Char.Alignment alignment) {
		if (alignment != Char.Alignment.ALLY) {
			for (Buff buff : new ArrayList<>(mob.buffs())) {
				if (buff instanceof AllyBuff) StatusEditor.remove(buff);
			}
		}
		mob.alignment = alignment;
		editedAlignment.add(mob);
	}

	static void setState(Mob mob, Mob.AiState state) {
		mob.state = state;
	}

	/**
	 * Lets the player pick a tile and sends [mob] there.
	 */
	static void chooseDestination(Mob mob) {
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null || !mob.isAlive()) return;
				moveTo(mob, cell);
			}

			@Override
			public String prompt() {
				return "Choose where " + mob.name() + " should go";
			}
		});
	}

	private static void moveTo(Mob mob, int cell) {
		try {
			mob.clearEnemy();
			mob.state = mob.WANDERING;
			Field target = Mob.class.getDeclaredField("target");
			target.setAccessible(true);
			target.setInt(mob, cell);
		} catch (Throwable e) {
			Log.e(TAG, "Could not move " + mob.getClass().getName(), e);
		}
	}

	/**
	 * Lets the player pick a tile and teleports [mob] there.
	 */
	static void chooseTeleport(Mob mob) {
		GameScene.selectCell(new CellSelector.Listener() {
			@Override
			public void onSelect(Integer cell) {
				if (cell == null || !mob.isAlive() || !FloorEditor.canStandOn(cell)) return;
				try {
					com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation.appear(mob, cell);
					com.shatteredpixel.shatteredpixeldungeon.Dungeon.observe();
					GameScene.updateFog();
				} catch (Throwable e) {
					Log.e(TAG, "Could not teleport " + mob.getClass().getName(), e);
				}
			}

			@Override
			public String prompt() {
				return "Choose where to teleport " + mob.name();
			}
		});
	}

	static void defeat(Mob mob) {
		try {
			mob.die(null);
		} catch (Throwable e) {
			Log.e(TAG, "Could not defeat " + mob.getClass().getName(), e);
		}
	}

	/**
	 * Called at the end of Char.storeInBundle.
	 */
	public static void store(Object character, Bundle bundle) {
		if (!(character instanceof Char) || !editedAlignment.contains(character)) return;
		Char.Alignment alignment = ((Char) character).alignment;
		if (alignment != null) bundle.put(KEY_ALIGNMENT, alignment.name());
	}

	/**
	 * Called at the start of Char.restoreFromBundle, which reuses its parameter registers before returning.
	 */
	public static void beforeRestore(Object character, Bundle bundle) {
		restoring.push(new Object[] { character, bundle });
	}

	/**
	 * Called at the end of Char.restoreFromBundle.
	 */
	public static void afterRestore() {
		if (restoring.isEmpty()) return;
		Object[] entry = restoring.pop();
		restore(entry[0], (Bundle) entry[1]);
	}

	private static void restore(Object character, Bundle bundle) {
		if (!(character instanceof Mob)) return;
		String value = bundle.getString(KEY_ALIGNMENT);
		if (value == null || value.isEmpty()) return;
		try {
			((Mob) character).alignment = Char.Alignment.valueOf(value);
			editedAlignment.add((Char) character);
		} catch (IllegalArgumentException ignored) {
		}
	}

	/**
	 * Called at the start of the WndInfoMob constructor, which reuses its parameter register before returning.
	 */
	public static void beforeMobInfo(Object mob) {
		infoMob = mob;
	}

	/**
	 * Called at the end of the WndInfoMob constructor. Adds an Edit button under the description.
	 */
	public static void afterMobInfo(Window window) {
		Object mob = infoMob;
		infoMob = null;
		if (!EditorSwitches.enemy() || !(mob instanceof Mob)) return;
		try {
			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton edit = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton("Edit", 7) {
				@Override
				public void onClick() {
					window.hide();
					EnemyWindow.open((Mob) mob, EnemyWindow.TAB_STATS, 0);
				}
			};
			BottomRow.add(window, edit);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the edit button", e);
		}
	}
}
