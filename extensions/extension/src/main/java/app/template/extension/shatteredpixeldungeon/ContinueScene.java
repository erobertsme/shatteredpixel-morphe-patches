package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.GameLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;

/**
 * Continues the game in progress from its save files without the game's loading screen. The files are read while the screen is blank
 * between the two scenes, the same way the loading screen reads them, then the game scene opens with its usual fade. If reading fails,
 * the game's own loading screen tries again and reports the problem.
 */
public class ContinueScene extends Scene {

	private static final String TAG = "ContinueScene";

	static void start() {
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		Game.switchScene(ContinueScene.class);
	}

	@Override
	public void create() {
		super.create();
		try {
			Actor.fixTime();
			Mob.clearHeldAllies();
			GameLog.wipe();
			int slot = GamesInProgress.curSlot;
			Dungeon.loadGame(slot);
			if (Dungeon.depth == -1) {
				Dungeon.depth = Statistics.deepestFloor;
				Dungeon.switchLevel(Dungeon.loadLevel(slot), -1);
			} else {
				Dungeon.switchLevel(Dungeon.loadLevel(slot), Dungeon.hero.pos);
			}
			Game.switchScene(GameScene.class);
		} catch (Throwable e) {
			Log.e(TAG, "Could not continue the game, so the loading screen tries", e);
			Game.switchScene(InterlevelScene.class);
		}
	}
}
