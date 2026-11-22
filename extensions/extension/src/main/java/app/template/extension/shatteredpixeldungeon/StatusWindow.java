package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;

/**
 * Edits one status a character has: its turns left, its extra number such as healing left or combo, whether it lasts forever for your
 * hero, or removes it.
 */
public class StatusWindow extends EditorWindow {

	private static final String INFO = "Edit status\n\n"
		+ "_Turns left:_ how long the status lasts.\n_Everything else:_ every number and on/off setting the status keeps, like Healing's healing left and heal per turn, or Combo's count. Long-press - or + to change a number by 10 steps, or tap it to type one.\n\n"
		+ "{STAR}_Infinite:_ for your hero, the status never runs out and comes straight back if something removes it.";

	private static final int MIN_NUMBER = -9999;
	private static final int MAX_NUMBER = 99999;
	private static final int DECIMALS = 2;
	private static final int HUNDRED = 100;
	private static final int DECIMAL_STEP = 10;
	private static final int SHORT_LABEL = 12;

	private final Buff buff;
	private final Runnable back;

	public StatusWindow(Buff buff, Runnable back) {
		super();
		this.buff = buff;
		this.back = back;
		addTitle(StatusEditor.nameOf(buff), INFO);
		boolean hero = buff.target == Dungeon.hero;
		boolean infinite = hero && InfiniteStatuses.isInfinite(buff.getClass());
		if (StatusEditor.isTurnsEditable(buff) && !infinite) {
			stepperIcon(new BuffIcon(buff, true));
			int turns = StatusEditor.turnsValue(buff);
			addStepper("Turns left", String.valueOf(turns), turns, 1, 1, 9999, value -> StatusEditor.setTurns(buff, value));
		}
		for (StatusEditor.Setting setting : StatusEditor.settings(buff)) addSetting(setting);
		if (hero) addCheckBox("Infinite", infinite, () -> InfiniteStatuses.setInfinite(buff, !infinite));
		pos += GAP;
		addRowButton("Remove", Icons.get(Icons.CLOSE), () -> {
			hide();
			BuffInfoActions.remove(buff);
			back.run();
		});
		finishWithBack(this::backToStatus);
	}

	/**
	 * A check box for an on/off setting, or a stepper for a number. Decimals step by a tenth and can be typed to two places.
	 */
	private void addSetting(StatusEditor.Setting setting) {
		if (setting.isSwitch()) {
			boolean on = StatusEditor.isOn(buff, setting.field);
			addCheckBox(setting.label, on, () -> StatusEditor.setOn(buff, setting.field, !on));
			return;
		}
		float value = StatusEditor.number(buff, setting.field);
		if (setting.label.length() > SHORT_LABEL) stepperLabelAbove();
		if (!setting.isDecimal()) {
			int whole = Math.round(value);
			addStepper(setting.label, String.valueOf(whole), whole, 1, MIN_NUMBER, MAX_NUMBER, number -> StatusEditor.setNumber(buff, setting.field, number));
			return;
		}
		int hundredths = Math.round(value * HUNDRED);
		stepperDecimals(DECIMALS);
		addStepper(setting.label, NumberPrompt.format(hundredths, DECIMALS), hundredths, DECIMAL_STEP, MIN_NUMBER * HUNDRED, MAX_NUMBER * HUNDRED,
			number -> StatusEditor.setNumber(buff, setting.field, number / (float) HUNDRED));
	}

	/**
	 * Goes back to the status's info window while the status is still there.
	 */
	private void backToStatus() {
		if (buff.target != null) {
			CharacterWindow.showBuffInfo(buff, back);
			return;
		}
		back.run();
	}

	@Override
	protected void reopen() {
		show(new StatusWindow(buff, back));
	}
}
