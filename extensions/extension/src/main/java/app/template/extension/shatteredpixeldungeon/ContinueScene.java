package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.GameLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;

/**
 * Continues the game in progress from its save files without the game's loading screen. The screen fades quickly to black, the files are
 * read the same way the loading screen reads them, then the game scene fades quickly in. If reading fails, the game's own loading screen
 * tries again and reports the problem.
 */
public class ContinueScene extends Scene {

	private static final String TAG = "ContinueScene";
	private static final float FADE_SECONDS = 0.2f;

	static void start() {
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		Scene current = Game.scene();
		if (current == null) {
			Game.switchScene(ContinueScene.class);
			return;
		}
		ScreenFade.out(current, FADE_SECONDS, () -> Game.switchScene(ContinueScene.class));
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
			PixelScene.noFade = true;
			Game.switchScene(GameScene.class, new Game.SceneChangeCallback() {
				@Override
				public void beforeCreate() {
				}

				@Override
				public void afterCreate() {
					ScreenFade.in(Game.scene(), FADE_SECONDS);
				}
			});
		} catch (Throwable e) {
			Log.e(TAG, "Could not continue the game, so the loading screen tries", e);
			Game.switchScene(InterlevelScene.class);
		}
	}
}
