package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;

public class HungerWindow extends EditorWindow {

	private static final int NOT_HUNGRY_COLOR = 0xFF7AD35C;
	private static final int HUNGRY_COLOR = 0xFFE8C547;
	private static final int STARVING_COLOR = 0xFFE06A5A;
	private static final int BAR_HEIGHT = 8;
	private static final int STARVING_SHARE = 60;

	private static Runnable backAction;

	public static void open(Runnable back) {
		backAction = back;
		Game.scene().addToFront(new HungerWindow());
	}

	public HungerWindow() {
		super();
		boolean disabled = HungerEditor.isDisabled();

		addTitle("Hunger");
		addCheckBox("Disable hunger", disabled, () -> HungerEditor.setDisabled(!HungerEditor.isDisabled()));
		addHint("Your hero never gets hungry. Turn it off again and hunger continues from where it was.");

		addSection("Stage lengths");
		int notHungry = HungerEditor.notHungryTurns();
		int hungry = HungerEditor.hungryTurns();
		addStepper("Not hungry", notHungry + " turns", notHungry, 10, HungerEditor.MIN_TURNS, HungerEditor.MAX_TURNS, HungerEditor::setNotHungryTurns, !disabled);
		addStepper("Hungry", hungry + " turns", hungry, 10, HungerEditor.MIN_TURNS, HungerEditor.MAX_TURNS, HungerEditor::setHungryTurns, !disabled);
		addBar(notHungry, hungry, disabled);
		addHint("Turns from eating to Hungry, and from Hungry to Starving. Game defaults: 300 and 150. Food keeps its normal value, so longer stages make meals last longer.");

		addSection("Starving");
		addCheckBox("Starving causes damage", HungerEditor.starvingDamages(), () -> HungerEditor.setStarvingDamages(!HungerEditor.starvingDamages()), !disabled);

		float half = (WIDTH - GAP) / 2f;
		addButton("Reset to defaults", 0, half, () -> {
			HungerEditor.resetDefaults();
			refresh();
		});
		addButton("Back", half + GAP, half, this::back);
		pos += ROW_HEIGHT + GAP;

		addHint("Settings apply to every run. Shadows, the Salt Cube and other hunger effects still stack on top.");
		finish();
	}

	private void addBar(int notHungry, int hungry, boolean disabled) {
		float total = notHungry + hungry + STARVING_SHARE * (notHungry + hungry) / 450f;
		float x = 0;
		x = addSegment(x, WIDTH * notHungry / total, NOT_HUNGRY_COLOR, disabled);
		x = addSegment(x, WIDTH * hungry / total, HUNGRY_COLOR, disabled);
		addSegment(x, WIDTH - x, STARVING_COLOR, disabled);
		pos += BAR_HEIGHT + GAP;

		RenderedTextBlock legend = PixelScene.renderTextBlock("Not hungry - Hungry - Starving", 6);
		legend.hardlight(CharacterWindow.DIM_COLOR);
		legend.setPos((WIDTH - legend.width()) / 2f, pos);
		add(legend);
		pos = legend.bottom() + 2 * GAP;
	}

	private float addSegment(float x, float width, int color, boolean disabled) {
		ColorBlock block = new ColorBlock(Math.max(1, width), BAR_HEIGHT, color);
		block.x = x;
		block.y = pos;
		if (disabled) block.alpha(0.3f);
		add(block);
		return x + width;
	}

	private void back() {
		hide();
		Runnable action = backAction;
		backAction = null;
		if (action != null) action.run();
	}

	@Override
	protected void reopen() {
		show(new HungerWindow());
	}
}
