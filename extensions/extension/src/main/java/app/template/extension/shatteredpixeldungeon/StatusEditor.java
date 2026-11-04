package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class StatusEditor {

	private static final String TAG = "StatusEditor";
	private static final int NO_ICON = 127;

	static final String[] HUNGER_STATES = { "Normal", "Hungry", "Starving" };
	private static final float[] HUNGER_LEVELS = { 0f, 300f, 450f };

	static final class Entry {
		final Class<? extends Buff> type;
		final Buff sample;
		final String name;
		final Buff.buffType kind;

		Entry(Class<? extends Buff> type, Buff sample, String name, Buff.buffType kind) {
			this.type = type;
			this.sample = sample;
			this.name = name;
			this.kind = kind;
		}
	}

	private static List<Entry> catalog;

	private StatusEditor() {
	}

	static synchronized List<Entry> catalog() {
		if (catalog != null) return catalog;
		List<Entry> entries = new ArrayList<>();
		for (String className : BuffClasses.NAMES) {
			Entry entry = load(className);
			if (entry != null) entries.add(entry);
		}
		Collections.sort(entries, (a, b) -> a.name.compareToIgnoreCase(b.name));
		catalog = entries;
		return catalog;
	}

	@SuppressWarnings("unchecked")
	private static Entry load(String className) {
		try {
			Class<?> type = Class.forName(className);
			if (!Buff.class.isAssignableFrom(type) || type == Hunger.class) return null;
			Buff sample = (Buff) type.getConstructor().newInstance();
			if (sample.icon() == NO_ICON) return null;
			return new Entry((Class<? extends Buff>) type, sample, nameOf(sample), sample.type);
		} catch (Throwable e) {
			return null;
		}
	}

	static List<Entry> catalog(Buff.buffType kind) {
		List<Entry> result = new ArrayList<>();
		for (Entry entry : catalog()) {
			Buff.buffType entryKind = entry.kind == null ? Buff.buffType.NEUTRAL : entry.kind;
			if (entryKind == kind) result.add(entry);
		}
		return result;
	}

	static List<Buff> active(Hero hero) {
		List<Buff> result = new ArrayList<>();
		for (Buff buff : new ArrayList<>(hero.buffs())) {
			if (buff instanceof Hunger) continue;
			if (iconOf(buff) == NO_ICON) continue;
			result.add(buff);
		}
		return result;
	}

	static boolean isActive(Hero hero, Class<? extends Buff> type) {
		for (Buff buff : hero.buffs()) {
			if (buff.getClass() == type) return true;
		}
		return false;
	}

	static void remove(Buff buff) {
		try {
			buff.detach();
		} catch (Throwable e) {
			Log.e(TAG, "Could not remove " + buff.getClass().getName(), e);
		}
		refreshIndicator();
	}

	static void removeNegative(Hero hero) {
		for (Buff buff : active(hero)) {
			if (buff.type == Buff.buffType.NEGATIVE) remove(buff);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void add(Hero hero, Class<? extends Buff> type, int duration) {
		try {
			if (FlavourBuff.class.isAssignableFrom(type)) {
				Buff.affect(hero, (Class) type, (float) duration);
				refreshIndicator();
				return;
			}

			Buff buff = Buff.affect(hero, type);
			if (buff instanceof Burning) {
				((Burning) buff).reignite(hero, duration);
				refreshIndicator();
				return;
			}
			applyDuration(buff, duration);
		} catch (Throwable e) {
			Log.e(TAG, "Could not add " + type.getName(), e);
		}
		refreshIndicator();
	}

	private static void applyDuration(Buff buff, int duration) {
		for (Class<?> type = buff.getClass(); type != null && type != Buff.class; type = type.getSuperclass()) {
			if (invoke(buff, type, "set", float.class, (float) duration)) return;
			if (invoke(buff, type, "set", int.class, duration)) return;
		}
	}

	private static boolean invoke(Buff buff, Class<?> type, String name, Class<?> parameter, Object value) {
		try {
			Method method = type.getDeclaredMethod(name, parameter);
			method.setAccessible(true);
			method.invoke(buff, value);
			return true;
		} catch (Throwable e) {
			return false;
		}
	}

	static int hungerState(Hero hero) {
		float level = hungerLevel(hero);
		if (level >= HUNGER_LEVELS[2]) return 2;
		if (level >= HUNGER_LEVELS[1]) return 1;
		return 0;
	}

	static void setHungerState(Hero hero, int state) {
		Hunger hunger = hunger(hero);
		if (hunger == null) return;
		int index = Math.max(0, Math.min(HUNGER_LEVELS.length - 1, state));
		try {
			Field field = Hunger.class.getDeclaredField("level");
			field.setAccessible(true);
			field.setFloat(hunger, HUNGER_LEVELS[index]);
		} catch (Throwable e) {
			Log.e(TAG, "Could not set hunger", e);
		}
		refreshIndicator();
	}

	private static float hungerLevel(Hero hero) {
		Hunger hunger = hunger(hero);
		if (hunger == null) return 0;
		try {
			Field field = Hunger.class.getDeclaredField("level");
			field.setAccessible(true);
			return field.getFloat(hunger);
		} catch (Throwable e) {
			return 0;
		}
	}

	private static Hunger hunger(Hero hero) {
		for (Buff buff : hero.buffs()) {
			if (buff instanceof Hunger) return (Hunger) buff;
		}
		return null;
	}

	static String nameOf(Buff buff) {
		try {
			String name = buff.name();
			if (name != null && !name.isEmpty() && !name.startsWith("!!!")) return capitalize(name);
		} catch (Throwable ignored) {
		}
		return buff.getClass().getSimpleName();
	}

	static String turnsOf(Buff buff) {
		try {
			String text = buff.iconTextDisplay();
			if (text != null && !text.isEmpty()) return text;
		} catch (Throwable ignored) {
		}
		return "";
	}

	private static int iconOf(Buff buff) {
		try {
			return buff.icon();
		} catch (Throwable e) {
			return NO_ICON;
		}
	}

	private static String capitalize(String text) {
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}

	private static void refreshIndicator() {
		try {
			BuffIndicator.refreshHero();
		} catch (Throwable ignored) {
		}
	}
}
