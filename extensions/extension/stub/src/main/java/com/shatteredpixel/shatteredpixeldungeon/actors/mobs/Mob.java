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
}
