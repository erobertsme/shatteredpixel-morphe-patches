package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;

public class Mob extends Char {
	public interface AiState {
	}

	public AiState SLEEPING;
	public AiState HUNTING;
	public AiState WANDERING;
	public AiState FLEEING;
	public AiState PASSIVE;
	public AiState state;
	public int EXP;
	public int defenseSkill;

	public com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite sprite() { throw new RuntimeException("Stub"); }
	public void clearEnemy() { throw new RuntimeException("Stub"); }
}
