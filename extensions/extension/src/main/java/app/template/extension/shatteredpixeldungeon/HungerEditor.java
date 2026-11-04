package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;

import java.util.Locale;

@SuppressWarnings("unused")
public final class HungerEditor {

	public static final int DEFAULT_NOT_HUNGRY = 300;
	public static final int DEFAULT_HUNGRY = 150;
	public static final int DEFAULT_FORMULA = 1000;
	public static final int DEFAULT_AMOUNT = 1;
	public static final int MIN_TURNS = 10;
	public static final int MAX_TURNS = 99999;
	public static final int MIN_FORMULA = 10;
	public static final int MAX_FORMULA = 99990;
	public static final int MAX_INTERVAL = 9999;
	public static final int MAX_AMOUNT = 999;
	public static final float[] PRESETS = { 0.75f, 0.5f, 0.3f };

	private static final float HUNGRY_LEVEL = 300f;
	private static final float STARVING_LEVEL = 450f;
	private static final String KEY_DISABLED = "hunger_disabled";
	private static final String KEY_NO_STARVE_DAMAGE = "hunger_no_starve_damage";
	private static final String KEY_NOT_HUNGRY = "hunger_not_hungry_turns";
	private static final String KEY_HUNGRY = "hunger_hungry_turns";
	private static final String KEY_FIXED = "hunger_starve_fixed";
	private static final String KEY_FORMULA = "hunger_starve_formula";
	private static final String KEY_INTERVAL = "hunger_starve_fixed_interval";
	private static final String KEY_AMOUNT = "hunger_starve_damage";

	private static float partialDamage;
	private static int fixedTurns;
	private static boolean dealingStarvingDamage;

	private HungerEditor() {
	}

	/**
	 * Replaced by the Hunger editor patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	static boolean isDisabled() {
		return CloverlordSettings.get(KEY_DISABLED);
	}

	static void setDisabled(boolean value) {
		CloverlordSettings.set(KEY_DISABLED, value);
	}

	static boolean starvingDamages() {
		return !CloverlordSettings.get(KEY_NO_STARVE_DAMAGE);
	}

	static void setStarvingDamages(boolean value) {
		CloverlordSettings.set(KEY_NO_STARVE_DAMAGE, !value);
	}

	static int notHungryTurns() {
		return orDefault(KEY_NOT_HUNGRY, DEFAULT_NOT_HUNGRY);
	}

	static void setNotHungryTurns(int value) {
		CloverlordSettings.setInt(KEY_NOT_HUNGRY, clamp(value, MIN_TURNS, MAX_TURNS));
	}

	static int hungryTurns() {
		return orDefault(KEY_HUNGRY, DEFAULT_HUNGRY);
	}

	static void setHungryTurns(int value) {
		CloverlordSettings.setInt(KEY_HUNGRY, clamp(value, MIN_TURNS, MAX_TURNS));
	}

	static void resetStages() {
		CloverlordSettings.setInt(KEY_NOT_HUNGRY, 0);
		CloverlordSettings.setInt(KEY_HUNGRY, 0);
	}

	static void applyStagePreset(float rate) {
		setNotHungryTurns(Math.round(DEFAULT_NOT_HUNGRY / rate));
		setHungryTurns(Math.round(DEFAULT_HUNGRY / rate));
	}

	static boolean usesFixedInterval() {
		return CloverlordSettings.get(KEY_FIXED);
	}

	static void setUsesFixedInterval(boolean value) {
		if (value && CloverlordSettings.getInt(KEY_INTERVAL) <= 0) setFixedInterval(Math.round(formulaInterval(maxHealth())));
		CloverlordSettings.set(KEY_FIXED, value);
	}

	static int formula() {
		return orDefault(KEY_FORMULA, DEFAULT_FORMULA);
	}

	static void setFormula(int value) {
		CloverlordSettings.setInt(KEY_FORMULA, clamp(value, MIN_FORMULA, MAX_FORMULA));
	}

	static int fixedInterval() {
		return orDefault(KEY_INTERVAL, 10);
	}

	static void setFixedInterval(int value) {
		CloverlordSettings.setInt(KEY_INTERVAL, clamp(value, 1, MAX_INTERVAL));
	}

	static int damageAmount() {
		return orDefault(KEY_AMOUNT, DEFAULT_AMOUNT);
	}

	static void setDamageAmount(int value) {
		CloverlordSettings.setInt(KEY_AMOUNT, clamp(value, 1, MAX_AMOUNT));
	}

	static void resetStarving() {
		setStarvingDamages(true);
		CloverlordSettings.set(KEY_FIXED, false);
		CloverlordSettings.setInt(KEY_FORMULA, 0);
		CloverlordSettings.setInt(KEY_INTERVAL, 0);
		CloverlordSettings.setInt(KEY_AMOUNT, 0);
	}

	static void applyStarvingPreset(float rate) {
		CloverlordSettings.set(KEY_FIXED, false);
		setFormula(Math.round(DEFAULT_FORMULA / rate / 10f) * 10);
	}

	static void resetDefaults() {
		setDisabled(false);
		resetStages();
		resetStarving();
	}

	static int maxHealth() {
		Hero hero = Dungeon.hero;
		return hero == null ? 100 : Math.max(1, hero.HT);
	}

	static float defaultInterval(int maxHealth) {
		return DEFAULT_FORMULA / (float) Math.max(1, maxHealth);
	}

	static float formulaInterval(int maxHealth) {
		return formula() / (float) Math.max(1, maxHealth);
	}

	static float starvingInterval(int maxHealth) {
		return usesFixedInterval() ? fixedInterval() : formulaInterval(maxHealth);
	}

	/**
	 * A short summary of the current starvation damage, such as "1 damage every 20 turns".
	 */
	static String describeStarving(int maxHealth) {
		if (isDisabled()) return "Hunger disabled";
		if (!starvingDamages()) return "No starvation damage";
		return damageAmount() + " damage every " + formatTurns(starvingInterval(maxHealth)) + " turns";
	}

	static String formatTurns(float turns) {
		if (Math.abs(turns - Math.round(turns)) < 0.05f) return String.valueOf(Math.round(turns));
		return String.format(Locale.ENGLISH, "%.1f", turns);
	}

	private static boolean usesCustomStarving() {
		return usesFixedInterval() || formula() != DEFAULT_FORMULA || damageAmount() != DEFAULT_AMOUNT;
	}

	/**
	 * Called before SaltCube.hungerGainMultiplier returns. Scales how fast hunger rises in the current stage.
	 */
	public static float adjustHungerGain(float value) {
		if (!EditorSwitches.hunger()) return value;
		if (isDisabled()) return 0f;
		if (Dungeon.hero == null) return value;
		float level = StatusEditor.hungerLevel(Dungeon.hero);
		if (level < HUNGRY_LEVEL) return value * HUNGRY_LEVEL / notHungryTurns();
		return value * (STARVING_LEVEL - HUNGRY_LEVEL) / hungryTurns();
	}

	/**
	 * Called at the start of Hero.damage. Returns true to ignore the game's own starvation damage.
	 */
	public static boolean blocksDamage(Object source) {
		if (!(source instanceof Hunger) || dealingStarvingDamage || !EditorSwitches.hunger()) return false;
		return isDisabled() || !starvingDamages() || usesCustomStarving();
	}

	/**
	 * Called at the start of Hunger.act. Deals starvation damage with the custom settings.
	 */
	public static void onHungerAct(Object hunger) {
		Hero hero = Dungeon.hero;
		if (!(hunger instanceof Hunger) || hero == null || !hero.isAlive() || !EditorSwitches.hunger()) return;
		if (isDisabled() || !starvingDamages() || !usesCustomStarving()) return;
		if (Dungeon.level != null && Dungeon.level.locked) return;
		if (!((Hunger) hunger).isStarving()) {
			partialDamage = 0;
			fixedTurns = 0;
			return;
		}

		int hits;
		if (usesFixedInterval()) {
			if (++fixedTurns < fixedInterval()) return;
			fixedTurns = 0;
			hits = 1;
		} else {
			partialDamage += 1f / formulaInterval(hero.HT);
			hits = (int) partialDamage;
			if (hits <= 0) return;
			partialDamage -= hits;
		}

		dealingStarvingDamage = true;
		try {
			hero.damage(hits * damageAmount(), hunger);
		} finally {
			dealingStarvingDamage = false;
		}
	}

	private static int orDefault(String key, int fallback) {
		int value = CloverlordSettings.getInt(key);
		return value <= 0 ? fallback : value;
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}
}
