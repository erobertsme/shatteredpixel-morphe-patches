package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.watabou.utils.SparseArray;

import java.util.HashMap;

public class Level {
	public boolean locked;
	public int[] map;
	public boolean[] passable;
	public SparseArray<Trap> traps;
	public HashMap<Class<? extends Blob>, Blob> blobs;
	public SparseArray<Heap> heaps;
	public boolean[] mapped;
	public boolean[] visited;
	public boolean[] discoverable;
	public boolean[] secret;
	public java.util.HashSet<com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob> mobs;
	public SparseArray<com.shatteredpixel.shatteredpixeldungeon.plants.Plant> plants;

	public int length() { throw new RuntimeException("Stub"); }
	public void discover(int cell) { throw new RuntimeException("Stub"); }
	public com.shatteredpixel.shatteredpixeldungeon.plants.Plant plant(com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Seed seed, int pos) { throw new RuntimeException("Stub"); }
	public void uproot(int pos) { throw new RuntimeException("Stub"); }

	public Heap drop(Item item, int cell) { throw new RuntimeException("Stub"); }
	public Trap setTrap(Trap trap, int pos) { throw new RuntimeException("Stub"); }
	public int width() { throw new RuntimeException("Stub"); }
	public static void set(int cell, int terrain) { throw new RuntimeException("Stub"); }
}
