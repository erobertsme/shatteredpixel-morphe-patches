package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;

public class HungerWindow extends EditorWindow {

	static final int NOT_HUNGRY_COLOR = 0xFF7AD35C;
	static final int HUNGRY_COLOR = 0xFFE8C547;
	static final int STARVING_COLOR = 0xFFE06A5A;
	private static final int BAR_HEIGHT = 8;
	private static final String[] TABS = { "Stages", "Starving" };
	private static final String[] PRESET_LABELS = { "Default", "75%", "50%", "30%" };

	private static Runnable backAction;
	private static int tab;

	public static void open(Runnable back) {
		backAction = back;
		Game.scene().addToFront(new HungerWindow());
	}

	public HungerWindow() {
		super();
		boolean disabled = HungerEditor.isDisabled();

		addTitle("Hunger");
		addCheckBox("Disable hunger", disabled, () -> HungerEditor.setDisabled(!HungerEditor.isDisabled()));
		addButtonRow(TABS, tab, index -> {
			tab = index;
			refresh();
		});
		pos += GAP;

		if (tab == 0) {
			buildStages(disabled);
		} else {
			buildStarving(disabled);
		}

		float half = (WIDTH - GAP) / 2f;
		addButton("Reset all", 0, half, () -> {
			HungerEditor.resetDefaults();
			refresh();
		});
		addButton("Back", half + GAP, half, this::back);
		pos += ROW_HEIGHT + GAP;
		addHint("Settings apply to every run. Shadows, the Salt Cube and other hunger effects still apply on top.");
		finish();
	}

	private void buildStages(boolean disabled) {
		addPresetRow(disabled, HungerEditor::resetStages, HungerEditor::applyStagePreset);

		int notHungry = HungerEditor.notHungryTurns();
		int hungry = HungerEditor.hungryTurns();
		addStepper("Not hungry", notHungry + " turns", notHungry, 10, HungerEditor.MIN_TURNS, HungerEditor.MAX_TURNS,
			HungerEditor::setNotHungryTurns, !disabled, () -> HungerEditor.setNotHungryTurns(HungerEditor.DEFAULT_NOT_HUNGRY));
		addStepper("Hungry", hungry + " turns", hungry, 10, HungerEditor.MIN_TURNS, HungerEditor.MAX_TURNS,
			HungerEditor::setHungryTurns, !disabled, () -> HungerEditor.setHungryTurns(HungerEditor.DEFAULT_HUNGRY));

		pos += GAP;
		pos = addHungerBar(this, 0, pos, WIDTH, notHungry, hungry, disabled) + GAP;
		addHint("Turns from eating until Hungry, then from Hungry until Starving. Defaults are 300 and 150. The percentage buttons make hunger rise at that rate of normal. Food keeps its normal value.");
	}

	private void buildStarving(boolean disabled) {
		boolean damages = HungerEditor.starvingDamages();
		boolean enabled = !disabled && damages;
		addCheckBox("Starving causes damage", damages, () -> HungerEditor.setStarvingDamages(!HungerEditor.starvingDamages()), !disabled);

		boolean fixed = HungerEditor.usesFixedInterval();
		addButtonRow(new String[] { "Formula", "Fixed" }, fixed ? 1 : 0, index -> {
			HungerEditor.setUsesFixedInterval(index == 1);
			refresh();
		});

		if (fixed) {
			int interval = HungerEditor.fixedInterval();
			addStepper("Every", interval + " turns", interval, 1, 1, HungerEditor.MAX_INTERVAL,
				HungerEditor::setFixedInterval, enabled, () -> HungerEditor.setFixedInterval(Math.round(HungerEditor.defaultInterval(HungerEditor.maxHealth()))));
		} else {
			int formula = HungerEditor.formula();
			addStepper("Formula", String.valueOf(formula), formula, 10, HungerEditor.MIN_FORMULA, HungerEditor.MAX_FORMULA,
				HungerEditor::setFormula, enabled, () -> HungerEditor.setFormula(HungerEditor.DEFAULT_FORMULA));
		}
		int amount = HungerEditor.damageAmount();
		addStepper("Damage", String.valueOf(amount), amount, 1, 1, HungerEditor.MAX_AMOUNT,
			HungerEditor::setDamageAmount, enabled, () -> HungerEditor.setDamageAmount(HungerEditor.DEFAULT_AMOUNT));

		addPresetRow(!enabled, HungerEditor::resetStarving, HungerEditor::applyStarvingPreset);

		int maxHealth = HungerEditor.maxHealth();
		RenderedTextBlock result = PixelScene.renderTextBlock("Now: " + HungerEditor.describeStarving(maxHealth) + " (" + maxHealth + " max health)", 6);
		result.maxWidth(WIDTH);
		result.hardlight(CharacterWindow.TITLE_COLOR);
		result.setPos(0, pos + GAP);
		add(result);
		pos = result.bottom() + 2 * GAP;

		if (fixed) {
			addHint("Fixed: you take damage every set number of turns, whatever your max health. Its reset uses the game's default for your max health. The percentage buttons switch back to Formula.");
		} else {
			addHint("Formula: turns between hits = formula / max health. The game uses 1000, so with 50 max health you take damage every 20 turns. A higher formula means longer between hits.");
		}
	}

	interface Preset {
		void apply(float rate);
	}

	private void addPresetRow(boolean disabled, Runnable reset, Preset preset) {
		float width = (WIDTH - (PRESET_LABELS.length - 1) * GAP) / (float) PRESET_LABELS.length;
		for (int i = 0; i < PRESET_LABELS.length; i++) {
			int index = i;
			addButton(PRESET_LABELS[i], i * (width + GAP), width, () -> {
				if (index == 0) {
					reset.run();
				} else {
					preset.apply(HungerEditor.PRESETS[index - 1]);
				}
				refresh();
			}).enable(!disabled);
		}
		pos += ROW_HEIGHT + GAP;
	}

	/**
	 * Draws the hunger stage bar and its legend into [group]. Returns the bottom of the legend.
	 */
	static float addHungerBar(Group group, float x, float y, float width, int notHungry, int hungry, boolean disabled) {
		float starvingShare = (notHungry + hungry) * 0.15f;
		float total = notHungry + hungry + starvingShare;
		float left = x;
		left = addSegment(group, left, y, width * notHungry / total, NOT_HUNGRY_COLOR, disabled);
		left = addSegment(group, left, y, width * hungry / total, HUNGRY_COLOR, disabled);
		addSegment(group, left, y, x + width - left, STARVING_COLOR, disabled);

		RenderedTextBlock legend = PixelScene.renderTextBlock("Not hungry " + notHungry + " - Hungry " + hungry + " - Starving", 6);
		legend.hardlight(CharacterWindow.DIM_COLOR);
		legend.setPos(x + (width - legend.width()) / 2f, y + BAR_HEIGHT + 2);
		group.add(legend);
		return legend.bottom();
	}

	private static float addSegment(Group group, float x, float y, float width, int color, boolean disabled) {
		ColorBlock block = new ColorBlock(Math.max(1, width), BAR_HEIGHT, color);
		block.x = x;
		block.y = y;
		if (disabled) block.alpha(0.3f);
		group.add(block);
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
