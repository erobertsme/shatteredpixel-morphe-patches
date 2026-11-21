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
		+ "_Turns left:_ how long the status lasts.\n_Amount:_ the number some statuses keep besides their turns, like healing left, combo or shield.\n\n"
		+ "{STAR}_Infinite:_ for your hero, the status never runs out and comes straight back if something removes it.";

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
		String amountLabel = StatusEditor.amountLabel(buff.getClass());
		if (amountLabel != null) {
			int amount = StatusEditor.amountValue(buff);
			addStepper(amountLabel, String.valueOf(amount), amount, 1, 0, 9999, value -> StatusEditor.setAmount(buff, value));
		}
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
