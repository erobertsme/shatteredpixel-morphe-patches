package app.template.extension.shatteredpixeldungeon;

import com.watabou.noosa.Game;

/**
 * Game moments that several features react to, so each feature updates only when something it shows has changed.
 */
@SuppressWarnings("unused")
public final class GameEvents {

	private GameEvents() {
	}

	/**
	 * Called at the end of GameScene.create.
	 */
	public static void onSceneCreated(Object scene) {
		QuantumChest.onSceneCreated(scene);
		ForceAttack.onSceneCreated(scene);
		CustomItemTags.onSceneCreated(scene);
	}

	/**
	 * Called at the end of QuickSlotButton.refresh, which the game runs whenever the hero's items change.
	 */
	public static void onItemsChanged() {
		CustomItemTags.refresh();
		QuickslotRow.verifyRow();
	}

	/**
	 * Called at the end of Hero.ready, when the hero waits for your next move.
	 */
	public static void onHeroReady() {
		Game.runOnRenderThread(ResumePath::refresh);
	}

	/**
	 * Called at the start of Hero.busy, when the hero starts an action.
	 */
	public static void onHeroBusy() {
		if (ResumePath.isShown()) Game.runOnRenderThread(ResumePath::clear);
		if (PathPreview.isShown()) Game.runOnRenderThread(PathPreview::clear);
	}

	/**
	 * Called at the end of GameScene.gameOver.
	 */
	public static void onGameOver() {
		ForceAttack.refreshTag();
		CustomItemTags.refresh();
		PathPreview.clear();
	}
}
