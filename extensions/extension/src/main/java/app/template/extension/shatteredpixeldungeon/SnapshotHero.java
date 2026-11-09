package app.template.extension.shatteredpixeldungeon;

import android.util.Log;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.QuickSlot;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

import java.io.ByteArrayInputStream;

/**
 * A copy of the hero read from a saved game file, for showing what the hero had at that point. It never joins the game. While it is being read,
 * the patch's own load hooks leave the game in progress alone.
 */
final class SnapshotHero {

	private static final String HERO = "hero";

	static volatile boolean restoring;

	private SnapshotHero() {
	}

	/**
	 * The hero stored in a game file's bytes, or null if it can't be read.
	 */
	static Hero read(byte[] gameFile) {
		if (gameFile == null) return null;
		QuickSlot live = Dungeon.quickslot;
		try {
			Bundle bundle = Bundle.read(new ByteArrayInputStream(gameFile));
			Bundle heroBundle = bundle == null ? null : bundle.getBundle(HERO);
			if (heroBundle == null) return null;
			Dungeon.quickslot = new QuickSlot();
			restoring = true;
			Hero hero = new Hero();
			hero.restoreFromBundle(heroBundle);
			return hero;
		} catch (Throwable e) {
			Log.e("SnapshotHero", "Could not read the hero", e);
			return null;
		} finally {
			restoring = false;
			Dungeon.quickslot = live;
		}
	}
}
