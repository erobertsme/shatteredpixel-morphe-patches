package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;

/**
 * Starts a new game from a game's seed as the hero you pick. The new game takes the first empty slot, so every other game stays as it is.
 */
public class SeedGameWindow extends EditorWindow {

	private static final String INFO = "Start a new game from seed\n\n"
		+ "Pick a hero to start a new game on this seed: the same dungeon, with your current challenges. The new game takes an empty slot, so "
		+ "your other games, including this one, are kept.";

	private final long seed;
	private final Runnable back;

	SeedGameWindow(long seed, Runnable back) {
		super();
		this.seed = seed;
		this.back = back;
		addTitle("Start a new game from seed", INFO);
		addHint("Seed " + DungeonSeed.convertToCode(seed));
		for (HeroClass heroClass : HeroClass.values()) {
			if (!heroClass.isUnlocked()) continue;
			addRowButton(InventoryEditor.capitalize(heroClass.title()), HeroSprite.avatar(heroClass, 0), () -> start(heroClass));
		}
		pos += GAP;
		finishWithBack(back);
	}

	private void start(HeroClass heroClass) {
		hide();
		if (GameSlots.startFromSeed(seed, heroClass)) return;
		back.run();
		show(new WndMessage("Every game slot is in use. Erase a game to make room for a new one."));
	}

	@Override
	protected void reopen() {
		show(new SeedGameWindow(seed, back));
	}
}
