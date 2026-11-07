package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;

/**
 * The Experience and Loot pages in Extras, QoL.
 */
public class RewardRulesWindow extends EditorWindow {

	private static final String EXPERIENCE_INFO = "Experience\n\nEvery kind of creature stops giving experience once your level is above its limit. Rats stop after level 5, for example, and scorpios after level 27. Bosses and summoned creatures never give any.\n\nNormal: the game's own rule.\n\nNo level limit: creatures always give their full experience.\n\nRaise the limit: each creature's limit goes up by the number of levels you choose.\n\nReduced past the limit: past the limit, creatures still give the percentage of their experience you choose, at least 1.\n\nMultiplier: all experience from creatures is multiplied, and the game's limit still applies.\n\nCreatures that never give experience are not changed.";
	private static final String LOOT_INFO = "Loot\n\nEvery kind of creature stops dropping items once your level is more than 2 above its experience limit. Rats stop dropping after level 7, for example.\n\nNormal: the game's own rule.\n\nNo level limit: creatures always roll for their drops.\n\nRaise the limit: each creature's limit goes up by the number of levels you choose.\n\nReduced past the limit: past the limit, creatures still roll for drops at the percentage of their normal chance you choose.\n\nMultiplier: every creature's drop chance is multiplied, and the game's limit still applies.\n\nBoss drops and creatures that never drop anything are not changed.";

	private final KillRewards.Kind kind;

	public RewardRulesWindow(KillRewards.Kind kind) {
		super();
		this.kind = kind;
		addTitle(kind.title, kind == KillRewards.Kind.EXPERIENCE ? EXPERIENCE_INFO : LOOT_INFO);

		int mode = KillRewards.mode(kind);
		addSection("Rule");
		for (int i = 0; i < KillRewards.MODES.length; i++) {
			int choice = i;
			RedButton button = addButton(KillRewards.MODES[i], 0, WIDTH, () -> {
				KillRewards.mode(kind, choice);
				refresh();
			});
			if (i == mode) button.textColor(CharacterWindow.TITLE_COLOR);
			pos += ROW_HEIGHT + GAP;
		}

		pos += GAP;
		addSection("Setting");
		if (mode == KillRewards.RAISE) {
			int value = KillRewards.raise(kind);
			addStepper("Extra levels", "+" + value, value, 1, 1, 30, v -> KillRewards.raise(kind, v), true, () -> KillRewards.raise(kind, KillRewards.DEFAULT_RAISE));
		} else if (mode == KillRewards.REDUCED) {
			int value = KillRewards.reduced(kind);
			addStepper(kind == KillRewards.Kind.EXPERIENCE ? "Experience" : "Drop chance", value + "%", value, 5, 5, 100, v -> KillRewards.reduced(kind, v), true, () -> KillRewards.reduced(kind, KillRewards.DEFAULT_REDUCED));
		} else if (mode == KillRewards.MULTIPLY) {
			int value = KillRewards.multiply(kind);
			addStepper("Multiplier", value + "%", value, 10, 10, 1000, v -> KillRewards.multiply(kind, v), true, () -> KillRewards.multiply(kind, KillRewards.DEFAULT_MULTIPLY));
		} else {
			addHint("Nothing to set for this rule.");
		}

		pos += GAP;
		addButton("Back", 0, WIDTH, () -> {
			hide();
			CloverlordMenu.show();
		});
		pos += ROW_HEIGHT;
		finish();
	}

	@Override
	protected void reopen() {
		show(new RewardRulesWindow(kind));
	}
}
