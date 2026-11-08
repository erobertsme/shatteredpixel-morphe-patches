package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Tag;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Outside a game, a stand-in game screen behind the tag style page: the status pane, the toolbar, your tags and
 * the game's own situational buttons, so tags can be placed as if in a game.
 */
final class MenuPreview {

	private static final String TOOLBAR = "interfaces/toolbar.png";
	private static final String STATUS = "interfaces/status_pane.png";
	private static final int TOOLBAR_HEIGHT = 26;
	private static final int QUICKSLOTS = 4;

	private static final List<Gizmo> parts = new ArrayList<>();

	private MenuPreview() {
	}

	static boolean show() {
		hide();
		Scene scene = Game.scene();
		if (scene == null || scene instanceof GameScene) return false;
		Camera ui = PixelScene.uiCamera;
		ColorBlock screen = new ColorBlock(ui.width, ui.height, 0xFF141414);
		add(scene, screen);
		add(scene, at(new Image(STATUS, 0, 0, 82, 38), 0, 0));

		float bottom = ui.height - TOOLBAR_HEIGHT;
		add(scene, at(new ColorBlock(ui.width, TOOLBAR_HEIGHT, 0xFF0C0C0C), 0, bottom));
		add(scene, at(new Image(TOOLBAR, 0, 0, 64, TOOLBAR_HEIGHT), 0, bottom));
		Image pack = at(new Image(TOOLBAR, 0, 0, 24, TOOLBAR_HEIGHT), ui.width - 24, bottom);
		add(scene, pack);
		Image packIcon = Icons.get(Icons.BACKPACK_LRG);
		add(scene, at(packIcon, pack.x + (24 - packIcon.width()) / 2f, bottom + (TOOLBAR_HEIGHT - packIcon.height()) / 2f));
		for (int i = 1; i <= QUICKSLOTS; i++) {
			add(scene, at(new Image(TOOLBAR, 64, 0, 22, 24), ui.width - 24 - i * 22, bottom + 2));
		}

		List<TagPlacement> placements = new ArrayList<>();
		placements.add(UndoTags.PLACEMENT);
		placements.add(ForceAttack.PLACEMENT);
		for (int id : CustomItemTags.ids()) placements.add(CustomItemTags.placement(id));
		for (TagPlacement placement : placements) {
			IconTag tag = new IconTag(() -> { });
			tag.camera = ui;
			if (placement.isAuto()) {
				int width = placement.tagWidth();
				int height = placement.tagHeight();
				tag.setRect(ui.width - width, bottom - height, width, height);
				tag.flip(false);
				tag.onTop(false);
				tag.style(placement);
			} else {
				placement.place(tag);
			}
			add(scene, tag);
		}
		return true;
	}

	private static <T extends com.watabou.noosa.Visual> T at(T visual, float x, float y) {
		visual.x = x;
		visual.y = y;
		return visual;
	}

	private static void add(Scene scene, Gizmo gizmo) {
		gizmo.camera = PixelScene.uiCamera;
		scene.add(gizmo);
		parts.add(gizmo);
	}

	static void hide() {
		for (Gizmo gizmo : parts) {
			gizmo.killAndErase();
			gizmo.destroy();
		}
		parts.clear();
	}
}
