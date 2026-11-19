package app.template.extension.shatteredpixeldungeon;

/**
 * How much the hero resists each kind of elemental harm, from none to immune.
 */
public class ResistanceWindow extends EditorWindow {

	private static final String INFO = "Resistances\n\nHow much less fire, cold, lightning, poison, corrosion, ooze, paralysis and magic affect your hero, added on top of anything else that protects you. Damage and how long an effect lasts both shrink by this much. Magic covers wands, magical attacks such as warlock and DM-200 bolts, magic traps and bombs, and magical debuffs like charm, weakness and hex.\n\n100% makes you immune: the effect can't touch you at all.\n\nSaved with this run, like the other modifiers.";
	private static final int STEP = 10;

	private final Runnable onBack;

	public ResistanceWindow(Runnable onBack) {
		super();
		this.onBack = onBack;
		addTitle("Resistances", INFO);
		for (HeroExtras.Element element : HeroExtras.Element.values()) {
			int value = HeroExtras.resistance(element);
			String display = value >= HeroExtras.IMMUNE ? "Immune" : value + "%";
			stepperIcon(StatIcons.buff(element.icon));
			addStepper(element.label, display, value, STEP, 0, HeroExtras.IMMUNE, newValue -> HeroExtras.resistance(element, newValue));
		}
		pos += GAP;
		float half = (WIDTH - GAP) / 2f;
		addButton("Reset", 0, half, () -> {
			for (HeroExtras.Element element : HeroExtras.Element.values()) HeroExtras.resistance(element, 0);
			refresh();
		});
		addButton("Back", half + GAP, half, this::back);
		pos += ROW_HEIGHT;
		finish();
	}

	private void back() {
		hide();
		if (onBack != null) onBack.run();
	}

	@Override
	public void onBackPressed() {
		back();
	}

	@Override
	protected void reopen() {
		show(new ResistanceWindow(onBack));
	}
}
