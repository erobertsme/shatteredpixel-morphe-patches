package app.template.extension.shatteredpixeldungeon;

public class ConfirmWindow extends EditorWindow {

	private static final String INFO = "Confirm actions\n\nWhen an action is ticked, tapping the map shows what will happen first: the path your hero will walk, the creature you will attack, or the danger you are stepping into. Tap Go, or the same tile again, to do it. Tap Cancel, or another tile, to change your mind.\n\nMoving and Attacking each have a distance. A move only asks when it is at least that many steps long, and an attack only asks when the target is at least that many tiles away. By default, single steps and attacks on adjacent enemies go ahead right away. Set Moving or Attacking to 1 to confirm those too.\n\nStepping into danger covers known traps, chasms, fire and harmful gas, and applies at any distance.\n\nButtons such as the attack and pick up buttons, and keyboard or controller movement, are never confirmed.";

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
			if (!action.hasDistance()) continue;
			int distance = ConfirmSettings.minDistance(action);
			addStepper("  From", distance + " " + action.distanceUnit, distance, 1, 1, ConfirmSettings.MAX_DISTANCE, value -> ConfirmSettings.setMinDistance(action, value), ConfirmSettings.isOn() && ConfirmSettings.confirms(action), () -> ConfirmSettings.setMinDistance(action, action.defaultDistance));
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
