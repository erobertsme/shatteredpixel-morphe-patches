package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Edits the gases, area effects and traps on the current floor.
 */
@SuppressWarnings("unused")
public final class FloorEditor {

	private static final String TAG = "FloorEditor";
	private static final String BLOBS = "com.shatteredpixel.shatteredpixeldungeon.actors.blobs.";
	private static final int EMPTY = 1;
	private static final int SECRET_TRAP = 17;
	private static final int TRAP = 18;
	private static final int INACTIVE_TRAP = 19;
	private static final int BUTTON_HEIGHT = 16;
	private static final int GAP = 3;

	static final int GASES = 1;
	static final int OTHER = 2;

	static final class Effect {
		final String className;
		final String name;
		final boolean gas;

		Effect(String className, String name, boolean gas) {
			this.className = className;
			this.name = name;
			this.gas = gas;
		}

		@SuppressWarnings("unchecked")
		Class<? extends Blob> type() {
			try {
				return (Class<? extends Blob>) Class.forName(BLOBS + className);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
	}

	static final Effect[] EFFECTS = {
		new Effect("ToxicGas", "Toxic gas", true),
		new Effect("CorrosiveGas", "Corrosive gas", true),
		new Effect("ParalyticGas", "Paralytic gas", true),
		new Effect("ConfusionGas", "Confusion gas", true),
		new Effect("StenchGas", "Stench gas", true),
		new Effect("SmokeScreen", "Smoke screen", true),
		new Effect("StormCloud", "Storm cloud", true),
		new Effect("Blizzard", "Blizzard", true),
		new Effect("Inferno", "Inferno", true),
		new Effect("Fire", "Fire", false),
		new Effect("Freezing", "Freezing", false),
		new Effect("Electricity", "Electricity", false),
		new Effect("Web", "Web", false),
		new Effect("Regrowth", "Regrowth", false),
	};

	static final String[] TRAPS = {
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.AlarmTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.BlazingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.BurningTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.ChillingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.ConfusionTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.CorrosionTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.CursingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisarmingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.DisintegrationTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.DistortionTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.ExplosiveTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlashingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.FlockTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.FrostTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.GatewayTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.GeyserTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.GnollRockfallTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrimTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.GrippingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.GuardianTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.OozeTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.PitfallTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.PoisonDartTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.RockfallTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.ShockingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.StormTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.SummoningTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.TeleportationTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.WarpingTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.WeakeningTrap",
		"com.shatteredpixel.shatteredpixeldungeon.levels.traps.WornDartTrap",
	};

	private static Object pendingTarget;

	private FloorEditor() {
	}

	static Level level() {
		return Dungeon.level;
	}

	static String effectName(Class<?> type) {
		for (Effect effect : EFFECTS) {
			if (effect.className.equals(type.getSimpleName())) return effect.name;
		}
		String name = type.getSimpleName().replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
		return InventoryEditor.capitalize(name);
	}

	static List<Effect> effects(int kind) {
		List<Effect> result = new ArrayList<>();
		for (Effect effect : EFFECTS) {
			if (effect.type() == null) continue;
			boolean matches = kind == GASES ? effect.gas : kind == OTHER ? !effect.gas : Favorites.isEffect(effect.className);
			if (matches) result.add(effect);
		}
		return result;
	}

	static List<Blob> activeEffects() {
		List<Blob> result = new ArrayList<>();
		Level level = level();
		if (level == null || level.blobs == null) return result;
		for (Blob blob : level.blobs.values()) {
			if (blob != null && blob.volume > 0) result.add(blob);
		}
		return result;
	}

	static int strengthAt(Blob blob, int cell) {
		if (blob.cur == null || cell < 0 || cell >= blob.cur.length) return 0;
		return blob.cur[cell];
	}

	static void clearHere(Blob blob, int cell) {
		blob.clear(cell);
	}

	static void clearAll(Blob blob) {
		blob.fullyClear();
	}

	static void clearAllEffects() {
		for (Blob blob : activeEffects()) blob.fullyClear();
	}

	static void addEffect(Effect effect, int cell, int amount) {
		Class<? extends Blob> type = effect.type();
		if (type == null || level() == null) return;
		try {
			GameScene.add(Blob.seed(cell, amount, type, level()));
		} catch (Throwable e) {
			Log.e(TAG, "Could not add " + effect.className, e);
		}
	}

	static Trap trapAt(int cell) {
		Level level = level();
		return level == null || level.traps == null ? null : level.traps.get(cell, null);
	}

	static List<Trap> traps() {
		Level level = level();
		if (level == null || level.traps == null) return Collections.emptyList();
		return new ArrayList<>(level.traps.valueList());
	}

	static String trapName(Trap trap) {
		try {
			return InventoryEditor.capitalize(trap.name());
		} catch (Throwable e) {
			return trap.getClass().getSimpleName();
		}
	}

	static void setActive(Trap trap, boolean active) {
		trap.active = active;
		retile(trap);
	}

	static void setVisible(Trap trap, boolean visible) {
		if (visible) {
			trap.reveal();
		} else if (trap.canBeHidden) {
			trap.hide();
		}
		retile(trap);
	}

	static void trigger(Trap trap) {
		try {
			trap.active = true;
			trap.trigger();
		} catch (Throwable e) {
			Log.e(TAG, "Could not trigger " + trap.getClass().getName(), e);
		}
	}

	static boolean canPlaceTrap(int cell) {
		Level level = level();
		return level != null && level.passable != null && cell >= 0 && cell < level.passable.length && level.passable[cell];
	}

	@SuppressWarnings("unchecked")
	static void placeTrap(String className, int cell, boolean hidden) {
		if (!canPlaceTrap(cell)) return;
		try {
			Trap trap = ((Class<? extends Trap>) Class.forName(className)).getConstructor().newInstance();
			level().setTrap(trap, cell);
			trap.active = true;
			trap.visible = !hidden || !trap.canBeHidden;
			retile(trap);
		} catch (Throwable e) {
			Log.e(TAG, "Could not place " + className, e);
		}
	}

	static void removeTrap(Trap trap) {
		Level level = level();
		if (level == null) return;
		level.traps.remove(trap.pos);
		Level.set(trap.pos, EMPTY);
		GameScene.updateMap(trap.pos);
	}

	static void revealAll() {
		for (Trap trap : traps()) setVisible(trap, true);
	}

	static void hideAll() {
		for (Trap trap : traps()) setVisible(trap, false);
	}

	static void disarmAll() {
		for (Trap trap : traps()) setActive(trap, false);
	}

	static void rearmAll() {
		for (Trap trap : traps()) setActive(trap, true);
	}

	static void removeAllTraps() {
		for (Trap trap : traps()) removeTrap(trap);
	}

	private static void retile(Trap trap) {
		int tile = !trap.active ? INACTIVE_TRAP : trap.visible ? TRAP : SECRET_TRAP;
		Level.set(trap.pos, tile);
		GameScene.updateMap(trap.pos);
	}

	static String trapState(Trap trap) {
		return (trap.visible ? "visible" : "hidden") + " - " + (trap.active ? "active" : "disarmed");
	}

	/**
	 * Called at the start of the WndInfoCell constructor.
	 */
	public static void beforeCellInfo(int cell) {
		pendingTarget = cell;
	}

	/**
	 * Called at the start of the WndInfoTrap constructor.
	 */
	public static void beforeTrapInfo(Object trap) {
		pendingTarget = trap;
	}

	/**
	 * Called at the end of the WndInfoCell and WndInfoTrap constructors. Adds an Edit tile button.
	 */
	public static void afterInfo(Window window) {
		Object target = pendingTarget;
		pendingTarget = null;
		if (!EditorSwitches.floor() || target == null || Dungeon.hero == null) return;
		int cell = target instanceof Trap ? ((Trap) target).pos : (Integer) target;
		try {
			Field widthField = Window.class.getDeclaredField("width");
			Field heightField = Window.class.getDeclaredField("height");
			widthField.setAccessible(true);
			heightField.setAccessible(true);
			int width = widthField.getInt(window);
			int height = heightField.getInt(window);

			com.shatteredpixel.shatteredpixeldungeon.ui.RedButton edit = new com.shatteredpixel.shatteredpixeldungeon.ui.RedButton("Edit tile", 7) {
				@Override
				public void onClick() {
					window.hide();
					TileWindow.open(cell, () -> reopenInfo(target));
				}
			};
			edit.setRect(0, height + GAP, width, BUTTON_HEIGHT);
			window.add(edit);
			window.resize(width, height + GAP + BUTTON_HEIGHT);
		} catch (Exception e) {
			Log.e(TAG, "Could not add the edit tile button", e);
		}
	}

	private static void reopenInfo(Object target) {
		try {
			if (target instanceof Trap) {
				Trap trap = (Trap) target;
				if (trapAt(trap.pos) == trap) {
					EditorWindow.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTrap(trap));
					return;
				}
				EditorWindow.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell(trap.pos));
				return;
			}
			EditorWindow.show(new com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoCell((Integer) target));
		} catch (Throwable e) {
			Log.e(TAG, "Could not reopen the tile info", e);
		}
	}
}
