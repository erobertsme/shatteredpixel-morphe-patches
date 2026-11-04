package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;

@SuppressWarnings("unused")
public final class HungerEditor {

	public static final int DEFAULT_NOT_HUNGRY = 300;
	public static final int DEFAULT_HUNGRY = 150;
	public static final int MIN_TURNS = 10;
	public static final int MAX_TURNS = 99999;

	private static final float HUNGRY_LEVEL = 300f;
	private static final float STARVING_LEVEL = 450f;
	private static final String KEY_DISABLED = "hunger_disabled";
	private static final String KEY_NO_STARVE_DAMAGE = "hunger_no_starve_damage";
	private static final String KEY_NOT_HUNGRY = "hunger_not_hungry_turns";
	private static final String KEY_HUNGRY = "hunger_hungry_turns";

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
		int value = CloverlordSettings.getInt(KEY_NOT_HUNGRY);
		return value <= 0 ? DEFAULT_NOT_HUNGRY : value;
	}

	static void setNotHungryTurns(int value) {
		CloverlordSettings.setInt(KEY_NOT_HUNGRY, Math.max(MIN_TURNS, Math.min(MAX_TURNS, value)));
	}

	static int hungryTurns() {
		int value = CloverlordSettings.getInt(KEY_HUNGRY);
		return value <= 0 ? DEFAULT_HUNGRY : value;
	}

	static void setHungryTurns(int value) {
		CloverlordSettings.setInt(KEY_HUNGRY, Math.max(MIN_TURNS, Math.min(MAX_TURNS, value)));
	}

	static void resetDefaults() {
		setDisabled(false);
		setStarvingDamages(true);
		CloverlordSettings.setInt(KEY_NOT_HUNGRY, 0);
		CloverlordSettings.setInt(KEY_HUNGRY, 0);
	}

	/**
	 * Called before SaltCube.hungerGainMultiplier returns. Scales how fast hunger rises in the current stage.
	 */
	public static float adjustHungerGain(float value) {
		if (isDisabled()) return 0f;
		if (Dungeon.hero == null) return value;
		float level = StatusEditor.hungerLevel(Dungeon.hero);
		if (level < HUNGRY_LEVEL) return value * HUNGRY_LEVEL / notHungryTurns();
		return value * (STARVING_LEVEL - HUNGRY_LEVEL) / hungryTurns();
	}

	/**
	 * Called at the start of Hero.damage. Returns true to ignore starvation damage.
	 */
	public static boolean blocksDamage(Object source) {
		return source instanceof Hunger && (isDisabled() || !starvingDamages());
	}
}
