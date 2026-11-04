package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.watabou.utils.Bundle;

/**
 * Per-run character edits that are stored inside the hero's save data.
 */
@SuppressWarnings("unused")
public final class HeroExtras {

	public static final int TIERS = 4;

	public enum Mod {
		ACCURACY("Accuracy", false),
		EVASION("Evasion", false),
		DAMAGE("Damage", false),
		ARMOR("Armor", false),
		ATTACK_SPEED("Attack speed", true),
		MOVE_SPEED("Move speed", true),
		REGEN("HP regen", true);

		public final String label;
		public final boolean percent;

		Mod(String label, boolean percent) {
			this.label = label;
			this.percent = percent;
		}

		public int step() {
			return percent ? 5 : 1;
		}

		public int min() {
			return percent ? -90 : -999;
		}

		public int max() {
			return percent ? 1000 : 999;
		}
	}

	private static final String PREFIX = "morphe_extras_";
	private static final int[] bonusTalentPoints = new int[TIERS + 1];
	private static final int[] mods = new int[Mod.values().length];
	private static final int INFINITE_EVASION = 1_000_000;

	private static Object bonusHero;
	private static int bonusTier;

	private HeroExtras() {
	}

	public static int bonusTalentPoints(int tier) {
		return bonusTalentPoints[tier];
	}

	public static void bonusTalentPoints(int tier, int value) {
		bonusTalentPoints[tier] = Math.max(0, Math.min(99, value));
	}

	public static int mod(Mod mod) {
		return mods[mod.ordinal()];
	}

	public static void mod(Mod mod, int value) {
		mods[mod.ordinal()] = Math.max(mod.min(), Math.min(mod.max(), value));
	}

	public static void resetMods() {
		for (int i = 0; i < mods.length; i++) mods[i] = 0;
	}

	public static boolean isTierLocked(Hero hero, int tier) {
		if (hero.lvl < Talent.tierLevelThresholds[tier] - 1) return true;
		if (tier == 3 && hero.subClass == HeroSubClass.NONE) return true;
		return tier == 4 && hero.armorAbility == null;
	}

	/**
	 * Called at the start of the Hero constructor.
	 */
	public static void reset() {
		for (int i = 0; i < bonusTalentPoints.length; i++) bonusTalentPoints[i] = 0;
		resetMods();
		InfiniteStatuses.reset();
	}

	/**
	 * Called at the start of Hero.storeInBundle.
	 */
	public static void store(Bundle bundle) {
		for (int tier = 1; tier <= TIERS; tier++) bundle.put(PREFIX + "talents_" + tier, bonusTalentPoints[tier]);
		for (Mod mod : Mod.values()) bundle.put(PREFIX + mod.name().toLowerCase(), mods[mod.ordinal()]);
		InfiniteStatuses.store(bundle);
	}

	/**
	 * Called at the start of Hero.restoreFromBundle.
	 */
	public static void restore(Bundle bundle) {
		reset();
		for (int tier = 1; tier <= TIERS; tier++) bonusTalentPoints[tier] = bundle.getInt(PREFIX + "talents_" + tier);
		for (Mod mod : Mod.values()) mods[mod.ordinal()] = bundle.getInt(PREFIX + mod.name().toLowerCase());
		InfiniteStatuses.restore(bundle);
	}

	/**
	 * Called at the start of Hero.bonusTalentPoints, which reuses its parameter registers before returning.
	 */
	public static void beforeBonusTalentPoints(Object hero, int tier) {
		bonusHero = hero;
		bonusTier = tier;
	}

	/**
	 * Called before each return of Hero.bonusTalentPoints.
	 */
	public static int adjustBonusTalentPoints(int value) {
		Object hero = bonusHero;
		int tier = bonusTier;
		bonusHero = null;
		if (!(hero instanceof Hero) || hero != Dungeon.hero) return value;
		if (tier < 1 || tier > TIERS) return value;
		if (isTierLocked((Hero) hero, tier)) return value;
		return value + bonusTalentPoints[tier];
	}

	public static int adjustAccuracy(int value) {
		return Math.max(1, value + mod(Mod.ACCURACY));
	}

	public static int adjustEvasion(int value) {
		if (value >= INFINITE_EVASION) return value;
		return Math.max(0, value + mod(Mod.EVASION));
	}

	public static int adjustDamage(int value) {
		return Math.max(0, value + mod(Mod.DAMAGE));
	}

	public static int adjustArmor(int value) {
		return Math.max(0, value + mod(Mod.ARMOR));
	}

	public static float adjustAttackDelay(float value) {
		return value / multiplier(Mod.ATTACK_SPEED);
	}

	public static float adjustMoveSpeed(float value) {
		return value * multiplier(Mod.MOVE_SPEED);
	}

	public static float adjustRegen(float value) {
		return value * multiplier(Mod.REGEN);
	}

	private static float multiplier(Mod mod) {
		return 1f + mod(mod) / 100f;
	}
}
