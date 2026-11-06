package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.watabou.noosa.Game;

import java.util.Locale;

/**
 * Every stat of a creature, from the Enhanced Bestiary's Stats button.
 */
public class StatsWindow extends EditorWindow {

	private static final String INFO = "Stats\n\nHealth, accuracy and evasion are the creature's own numbers. Accuracy and evasion are compared with yours each time someone attacks: the higher the attacker's accuracy against the defender's evasion, the more likely the hit.\n\nDamage and armor are the lowest and highest rolls. Armor is taken off the damage of every hit it receives.\n\nSpeeds are compared with a normal creature: 2x moves or attacks twice as often, 0.5x half as often.\n\nExperience is what you earn for defeating it, until you reach the level shown.\n\nIn the bestiary the stats are for a fresh creature, without buffs, champion powers or anything else that changes them in a game.";

	private final Mob mob;

	static void open(Mob mob) {
		Game.scene().addToFront(new StatsWindow(mob));
	}

	public StatsWindow(Mob mob) {
		super();
		this.mob = mob;
		boolean live = EnhancedBestiary.isLive(mob);
		addTitle(name(mob), INFO);

		addSection("Combat");
		row("Health", live ? mob.HP + " / " + mob.HT : String.valueOf(mob.HT));
		row("Accuracy", safe(() -> String.valueOf(mob.attackSkill(Dungeon.hero))));
		row("Evasion", String.valueOf(mob.defenseSkill));
		row("Damage", range(EnhancedBestiary.range(mob::damageRoll)));
		row("Armor", range(EnhancedBestiary.range(mob::drRoll)));
		row("Attack speed", safe(() -> speed(1f / mob.attackDelay())));
		row("Move speed", safe(() -> speed(mob.speed())));
		row("Sight", mob.viewDistance + " tiles");

		addSection("Other");
		row("Experience", mob.EXP + " (until level " + mob.maxLvl + ")");
		row("Drops", EnhancedBestiary.lootText(mob));
		if (live) {
			row("Alignment", mob.alignment == null ? "Unknown" : titleCase(mob.alignment.name()));
			row("State", EnhancedBestiary.stateText(mob));
		} else {
			row("Normally", mob.alignment == Char.Alignment.ALLY ? "Ally" : mob.alignment == Char.Alignment.NEUTRAL ? "Neutral" : "Enemy");
		}

		pos += GAP;
		addButton("Back", 0, WIDTH, this::hide);
		pos += ROW_HEIGHT;
		finish();
	}

	private void row(String label, String value) {
		RenderedTextBlock left = PixelScene.renderTextBlock(label, 6);
		left.setPos(0, pos);
		add(left);
		RenderedTextBlock right = PixelScene.renderTextBlock(value, 6);
		right.maxWidth((int) (WIDTH - left.width() - 2 * GAP));
		right.setPos(WIDTH - right.width(), pos);
		add(right);
		pos = Math.max(left.bottom(), right.bottom()) + GAP;
	}

	private static String name(Mob mob) {
		try {
			return Messages.titleCase(mob.name());
		} catch (Throwable e) {
			return "Creature";
		}
	}

	private static String range(int[] values) {
		if (values == null) return "Unknown";
		return values[0] == values[1] ? String.valueOf(values[0]) : values[0] + " - " + values[1];
	}

	private static String speed(float value) {
		if (Math.abs(value - Math.round(value)) < 0.01f) return Math.round(value) + "x";
		return String.format(Locale.ROOT, "%.2fx", value);
	}

	private static String titleCase(String text) {
		String lower = text.toLowerCase(Locale.ROOT);
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

	interface Text {
		String get();
	}

	private static String safe(Text text) {
		try {
			return text.get();
		} catch (Throwable e) {
			return "Unknown";
		}
	}

	@Override
	protected void reopen() {
		show(new StatsWindow(mob));
	}
}
