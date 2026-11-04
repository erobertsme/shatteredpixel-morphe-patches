package app.template.extension.shatteredpixeldungeon;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class CloverlordSettings {

	private static final String PREFS = "cloverlord";
	private static final Map<String, Boolean> cache = new HashMap<>();

	private CloverlordSettings() {
	}

	public static synchronized boolean get(String key) {
		Boolean cached = cache.get(key);
		if (cached != null) return cached;

		SharedPreferences prefs = prefs();
		boolean value = prefs != null && prefs.getBoolean(key, false);
		cache.put(key, value);
		return value;
	}

	public static synchronized void set(String key, boolean value) {
		cache.put(key, value);
		SharedPreferences prefs = prefs();
		if (prefs == null) return;
		prefs.edit().putBoolean(key, value).apply();
	}

	private static SharedPreferences prefs() {
		Context context = SaveLocation.getContext();
		if (context == null) return null;
		return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}
}
