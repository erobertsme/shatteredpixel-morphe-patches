package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class CreatureCatalog {

	static final int FAVORITES = 0;
	static final int ENEMIES = 1;
	static final int NPCS = 2;
	static final int OTHER = 3;

	static final class Entry {
		final Class<? extends Mob> type;
		final String name;
		final int kind;

		Entry(Class<? extends Mob> type, String name, int kind) {
			this.type = type;
			this.name = name;
			this.kind = kind;
		}
	}

	private static List<Entry> catalog;

	private CreatureCatalog() {
	}

	static synchronized List<Entry> catalog() {
		if (catalog != null) return catalog;
		Map<String, Entry> byName = new LinkedHashMap<>();
		for (String className : MobClasses.NAMES) {
			Entry entry = load(className);
			if (entry == null) continue;
			String key = entry.name.toLowerCase(Locale.ENGLISH) + "#" + entry.kind;
			Entry existing = byName.get(key);
			if (existing == null || entry.type.getName().length() < existing.type.getName().length()) byName.put(key, entry);
		}
		List<Entry> entries = new ArrayList<>(byName.values());
		Collections.sort(entries, (a, b) -> a.name.compareToIgnoreCase(b.name));
		catalog = entries;
		return catalog;
	}

	static List<Entry> catalog(int kind) {
		List<Entry> result = new ArrayList<>();
		for (Entry entry : catalog()) {
			boolean matches = kind == FAVORITES ? Favorites.isCreature(entry.type.getName()) : entry.kind == kind;
			if (matches) result.add(entry);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Entry load(String className) {
		try {
			Class<?> type = Class.forName(className);
			if (!Mob.class.isAssignableFrom(type)) return null;
			Mob sample = (Mob) type.getConstructor().newInstance();
			String name = sample.name();
			if (name == null || name.isEmpty() || name.startsWith("!!!")) return null;
			int kind = className.contains(".actors.mobs.npcs.") ? NPCS : className.startsWith("com.shatteredpixel.shatteredpixeldungeon.actors.mobs.") ? ENEMIES : OTHER;
			return new Entry((Class<? extends Mob>) type, InventoryEditor.capitalize(name), kind);
		} catch (Throwable e) {
			return null;
		}
	}
}
