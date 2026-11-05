package com.shatteredpixel.shatteredpixeldungeon.actors;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;

import java.util.LinkedHashSet;

public class Char {
	public enum Alignment { ENEMY, NEUTRAL, ALLY }

	public Alignment alignment;
	public int pos;
	public boolean[] fieldOfView;
	public int HP;
	public int HT;

	public void damage(int dmg, Object src) { throw new RuntimeException("Stub"); }
	public void die(Object src) { throw new RuntimeException("Stub"); }
	public String name() { throw new RuntimeException("Stub"); }
	public boolean isAlive() { throw new RuntimeException("Stub"); }
	public int attackSkill(Char target) { throw new RuntimeException("Stub"); }
	public int defenseSkill(Char enemy) { throw new RuntimeException("Stub"); }
	public float speed() { throw new RuntimeException("Stub"); }
	public synchronized LinkedHashSet<Buff> buffs() { throw new RuntimeException("Stub"); }
	public int shielding() { throw new RuntimeException("Stub"); }
}
