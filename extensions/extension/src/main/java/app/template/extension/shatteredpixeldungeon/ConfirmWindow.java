package app.template.extension.shatteredpixeldungeon;

public class ConfirmWindow extends EditorWindow {

	private static final String INFO = "Confirm actions\n\nWhen an action is ticked, tapping the map shows what will happen first: the path your hero will walk, the creature you will attack, or the danger you are stepping into. Tap Go, or the same tile again, to do it. Tap Cancel, or another tile, to change your mind.\n\nStepping into danger covers known traps, chasms, fire and harmful gas, and applies even to single steps.\n\nButtons such as the attack and pick up buttons, and keyboard or controller movement, are never confirmed.";

	public ConfirmWindow() {
		super();
		addTitle("Confirm actions", INFO);
		addCheckBox("Confirm actions", ConfirmSettings.isOn(), () -> {
			ConfirmSettings.setOn(!ConfirmSettings.isOn());
			if (!ConfirmSettings.isOn()) PathPreview.clear();
		});
		pos += GAP;
		addSection("Ask before");
		for (ConfirmSettings.Action action : ConfirmSettings.Action.values()) {
			addCheckBox(action.label, ConfirmSettings.confirms(action), () -> ConfirmSettings.setConfirms(action, !ConfirmSettings.confirms(action)), ConfirmSettings.isOn());
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
		show(new ConfirmWindow());
	}
}
