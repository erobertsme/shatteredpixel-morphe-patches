package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent;
import com.watabou.noosa.Game;

import java.util.List;

public class TalentSwapWindow extends Window {

	private static final int WIDTH = CharacterWindow.WIDTH;
	private static final int ROW_HEIGHT = CharacterWindow.ROW_HEIGHT;
	private static final int GAP = CharacterWindow.GAP;

	private final Talent replacing;
	private final int tier;
	private float pos;

	public TalentSwapWindow(Talent replacing, int tier, HeroClass selectedClass) {
		super();
		this.replacing = replacing;
		this.tier = tier;

		Hero hero = Dungeon.hero;
		HeroClass shown = selectedClass != null ? selectedClass : hero.heroClass;

		RenderedTextBlock title = PixelScene.renderTextBlock("Replace " + replacing.title(), 9);
		title.hardlight(CharacterWindow.TITLE_COLOR);
		title.setPos(0, 0);
		add(title);
		pos = title.bottom() + 2 * GAP;

		HeroClass[] classes = HeroClass.values();
		int perRow = 3;
		float width = (WIDTH - (perRow - 1) * GAP) / (float) perRow;
		for (int i = 0; i < classes.length; i++) {
			HeroClass heroClass = classes[i];
			RedButton button = new RedButton(heroClass.title(), 6) {
				@Override
				public void onClick() {
					if (heroClass == shown) return;
					hide();
					Game.scene().addToFront(new TalentSwapWindow(replacing, tier, heroClass));
				}
			};
			if (heroClass == shown) button.textColor(CharacterWindow.TITLE_COLOR);
			button.setRect((i % perRow) * (width + GAP), pos + (i / perRow) * (ROW_HEIGHT + GAP), width, ROW_HEIGHT);
			add(button);
		}
		pos += ((classes.length + perRow - 1) / perRow) * (ROW_HEIGHT + GAP) + GAP;

		List<Talent> options = TalentEditor.swapOptions(hero, tier, shown);
		if (options.isEmpty()) addHint("No other tier " + tier + " talents from this class.");
		for (Talent option : options) addOption(hero, option);

		addHint("Tap to swap. Long-press to read a talent. Spent points carry over.");

		RedButton cancel = new RedButton("Cancel", 7) {
			@Override
			public void onClick() {
				back();
			}
		};
		cancel.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(cancel);
		pos += ROW_HEIGHT;

		resize(WIDTH, (int) pos);
	}

	private void addOption(Hero hero, Talent option) {
		RedButton button = new RedButton(option.title(), 6) {
			@Override
			public void onClick() {
				TalentEditor.swap(hero, tier, replacing, option);
				back();
			}

			@Override
			public boolean onLongClick() {
				Game.scene().addToFront(new WndInfoTalent(option, 0, null));
				return true;
			}
		};
		button.icon(new TalentIcon(option));
		button.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(button);
		pos += ROW_HEIGHT + GAP;
	}

	private void addHint(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.maxWidth(WIDTH);
		block.hardlight(CharacterWindow.DIM_COLOR);
		block.setPos(0, pos);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	private void back() {
		hide();
		CharacterWindow.open(CharacterWindow.TAB_TALENTS, tier);
	}
}
