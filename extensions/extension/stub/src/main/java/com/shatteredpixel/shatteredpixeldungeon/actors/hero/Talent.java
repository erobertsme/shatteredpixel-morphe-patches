package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public enum Talent {
	HEARTY_MEAL,
	ENHANCED_RINGS,
	LONGRANGE_WARP;

	public static int[] tierLevelThresholds;

	public String title() { throw new RuntimeException("Stub"); }
	public int maxPoints() { throw new RuntimeException("Stub"); }
	public static void initClassTalents(HeroClass cls, ArrayList<LinkedHashMap<Talent, Integer>> talents) { throw new RuntimeException("Stub"); }
	public static void onTalentUpgraded(Hero hero, Talent talent) { throw new RuntimeException("Stub"); }
}
