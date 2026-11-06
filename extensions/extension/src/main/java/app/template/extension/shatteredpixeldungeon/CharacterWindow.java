package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndGame;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoBuff;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoTalent;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.input.PointerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CharacterWindow extends Window {

	static final int WIDTH = 128;
	private static final int TALENT_ROW_HEIGHT = 18;
	static final int ROW_HEIGHT = 16;
	static final int GAP = 3;
	static final int TITLE_COLOR = 0xFFFF44;
	static final int DIM_COLOR = 0x999999;
	static final int POSITIVE_COLOR = 0x7AD35C;
	static final int NEGATIVE_COLOR = 0xE06A5A;
	static final int INFO_SIZE = 16;
	private static final int STEP_WIDTH = 15;
	private static final int VALUE_WIDTH = 34;

	public static final int TAB_STATS = 0;
	public static final int TAB_TALENTS = 1;
	public static final int TAB_MODIFIERS = 2;
	public static final int TAB_STATUS = 3;
	private static final String[] TAB_NAMES = { "Stats", "Talents", "Mods", "Status" };
	private static final String[] TAB_INFO = {
		"Stats\n\nTap a number to type a value, with Min and Max buttons. Long-press - or + to change by 10.\n\nMax health is saved as a bonus, so it survives level-ups. Shield adds a Barrier or drains your shields.\n\nThe totals at the bottom are read-only. Highlighted values include your modifiers.",
		"Talents\n\nPick a tier at the top. Bonus points add extra points to that tier.\n\n- and + remove or add a point. Adding needs an unspent point in that tier.\n\nThe swap button replaces a talent like a Scroll of Metamorphosis. Tier 4 talents depend on your armor ability and can't be swapped.\n\nSubclass, at the bottom of tier 3, and Armor Ability, at the bottom of tier 4, change your subclass or armor ability. The old one's talents, and the points in them, are replaced by the new one's.\n\nTap or long-press a talent to read it.",
		"Mods\n\nModifiers are added on top of everything else and are saved with this run.\n\nNumbers change by 1, speeds and regeneration by 5%. Long-press - or + to change by 10 steps, or tap a value to type it.\n\nThe regeneration modifier has no effect while a boss floor is locked.",
		"Status\n\nTap the number to change how many turns are left.\n\n{STAR}The star makes a status infinite: it never runs out and comes back straight away if something removes it. Bright means on.\n\nX removes a status. Add Status adds a new one and Remove Negative clears every negative status.\n\n{INFO}Tap i or long-press a status to read it.\n\nThe hunger buttons set how hungry you are. Well Fed also gives the Well Fed effect, as if you had just eaten a meat pie.",
	};

	interface IntSetter {
		void set(int value);
	}

	private final int tab;
	private final int tier;
	private final int page;
	private float pos;

	public CharacterWindow() {
		this(TAB_STATS, 1);
	}

	public CharacterWindow(int tab, int tier) {
		this(tab, tier, 0);
	}

	public CharacterWindow(int tab, int tier, int page) {
		super();
		this.tab = tab;
		this.tier = tier;
		this.page = page;

		Hero hero = Dungeon.hero;
		addTabs();
		if (hero != null) {
			if (tab == TAB_STATS) buildStats(hero);
			if (tab == TAB_TALENTS) buildTalents(hero);
			if (tab == TAB_MODIFIERS) buildModifiers(hero);
			if (tab == TAB_STATUS) buildStatus(hero);
		}
		addBackButton();
		resize(WIDTH, (int) pos);
	}

	private void addBackButton() {
		pos += GAP;
		RedButton back = new RedButton("Back", 7) {
			@Override
			public void onClick() {
				hide();
				Runnable action = backAction;
				backAction = null;
				if (action != null) {
					action.run();
				} else {
					Game.scene().addToFront(new WndGame());
				}
			}
		};
		back.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(back);
		pos += ROW_HEIGHT;
	}

	private static Runnable backAction;

	/**
	 * Opens the editor and remembers where its Back button returns to.
	 */
	public static void openFrom(Runnable back, int tab, int tier) {
		backAction = back;
		open(tab, tier);
	}

	/**
	 * Replaced by the Character editor patch to return true.
	 */
	public static boolean isAvailable() {
		return false;
	}

	public static void open(int tab, int tier) {
		Game.scene().addToFront(new CharacterWindow(tab, tier));
	}

	public static void open(int tab, int tier, int page) {
		Game.scene().addToFront(new CharacterWindow(tab, tier, page));
	}

	private void reopen(int newTab, int newTier) {
		hide();
		open(newTab, newTier);
	}

	private void reopenPage(int newPage) {
		hide();
		open(tab, tier, newPage);
	}

	private void refresh() {
		hide();
		open(tab, tier, page);
	}

	private void addTabs() {
		float tabsWidth = WIDTH - InfoButton.SIZE - GAP;
		float width = (tabsWidth - (TAB_NAMES.length - 1) * GAP) / (float) TAB_NAMES.length;
		InfoButton.add(this, WIDTH, 0, TAB_INFO[tab]);
		for (int i = 0; i < TAB_NAMES.length; i++) {
			final int index = i;
			RedButton button = new RedButton(TAB_NAMES[i], 6) {
				@Override
				public void onClick() {
					if (index != tab) reopen(index, tier);
				}
			};
			if (i == tab) button.textColor(TITLE_COLOR);
			button.setRect(i * (width + GAP), 0, width, ROW_HEIGHT);
			add(button);
		}
		pos = ROW_HEIGHT + 2 * GAP;
	}

	private void buildStats(Hero hero) {
		RenderedTextBlock title = PixelScene.renderTextBlock(("Level " + hero.lvl + " " + hero.className()).toUpperCase(Locale.ENGLISH), 9);
		title.hardlight(TITLE_COLOR);
		title.setPos(0, pos);
		add(title);
		pos = title.bottom() + 2 * GAP;

		addStepper("Health", String.valueOf(hero.HP), hero.HP, 1, hero.HT, value -> hero.HP = clamp(value, 1, hero.HT));
		addStepper("Max health", String.valueOf(hero.HT), hero.HT, 1, 999, value -> {
			int target = Math.max(1, value);
			hero.HTBoost += target - hero.HT;
			hero.updateHT(false);
		});
		addStepper("Level", String.valueOf(hero.lvl), hero.lvl, 1, 30, value -> {
			int previous = hero.lvl;
			hero.lvl = clamp(value, 1, 30);
			hero.exp = Math.min(hero.exp, hero.maxExp() - 1);
			hero.updateHT(hero.lvl > previous);
		});
		addStepper("Experience", hero.exp + " / " + hero.maxExp(), hero.exp, 0, hero.maxExp() - 1, value -> hero.exp = clamp(value, 0, hero.maxExp() - 1));
		addStepper("Strength", String.valueOf(hero.STR), hero.STR, 1, 99, value -> hero.STR = clamp(value, 1, 99));
		int shield = hero.shielding();
		addStepper("Shield", String.valueOf(shield), shield, 0, 999, value -> StatusEditor.setShield(hero, clamp(value, 0, 999)));

		addSection("Current totals");
		int strBonus = hero.STR() - hero.STR;
		addStat("Strength", strBonus == 0 ? String.valueOf(hero.STR) : hero.STR + (strBonus > 0 ? " + " : " - ") + Math.abs(strBonus));
		addStat("Accuracy", safe(() -> String.valueOf(hero.attackSkill(null))) + modSuffix(HeroExtras.Mod.ACCURACY));
		addStat("Evasion", safe(() -> String.valueOf(hero.defenseSkill(null))) + modSuffix(HeroExtras.Mod.EVASION));
		addStat("Damage", safe(() -> damageRange(hero)) + modSuffix(HeroExtras.Mod.DAMAGE));
		addStat("Armor", safe(() -> armorRange(hero)) + modSuffix(HeroExtras.Mod.ARMOR));
		addStat("Attack speed", safe(() -> format(1f / hero.attackDelay()) + "x") + modSuffix(HeroExtras.Mod.ATTACK_SPEED));
		addStat("Move speed", safe(() -> format(hero.speed()) + "x") + modSuffix(HeroExtras.Mod.MOVE_SPEED));
		addStat("Shield", safe(() -> String.valueOf(hero.shielding())));
		addStat("Gold / Energy", Dungeon.gold + " / " + Dungeon.energy);
	}

	private void buildTalents(Hero hero) {
		float width = (WIDTH - 3 * GAP) / 4f;
		for (int t = 1; t <= HeroExtras.TIERS; t++) {
			final int index = t;
			RedButton button = new RedButton("Tier " + t, 6) {
				@Override
				public void onClick() {
					if (index != tier) reopen(tab, index);
				}
			};
			if (t == tier) button.textColor(TITLE_COLOR);
			button.setRect((t - 1) * (width + GAP), pos, width, ROW_HEIGHT);
			add(button);
		}
		pos += ROW_HEIGHT + 2 * GAP;

		if (HeroExtras.isTierLocked(hero, tier)) {
			addHint(lockedReason(tier));
			addClassChoices(hero);
			return;
		}

		addStat("Unspent points", String.valueOf(hero.talentPointsAvailable(tier)));
		int bonus = HeroExtras.bonusTalentPoints(tier);
		addStepper("Bonus points", "+" + bonus, bonus, 0, 99, value -> HeroExtras.bonusTalentPoints(tier, value));
		pos += GAP;

		ArrayList<Talent> talents = new ArrayList<>(hero.talents.get(tier - 1).keySet());
		for (Talent talent : talents) addTalentRow(hero, talent);
		addClassChoices(hero);

	}

	/**
	 * The subclass buttons at the bottom of tier 3, and the armor ability buttons at the bottom of tier 4.
	 */
	private void addClassChoices(Hero hero) {
		if (tier == 3) {
			HeroSubClass[] subclasses = hero.heroClass.subClasses();
			String[] labels = new String[subclasses.length + 1];
			int selected = 0;
			labels[0] = "None";
			for (int i = 0; i < subclasses.length; i++) {
				labels[i + 1] = InventoryEditor.capitalize(subclasses[i].title());
				if (hero.subClass == subclasses[i]) selected = i + 1;
			}
			addSection("Subclass");
			addChoiceRow(labels, selected, index -> TalentEditor.setSubclass(hero, index == 0 ? HeroSubClass.NONE : subclasses[index - 1]));
			return;
		}
		if (tier != 4) return;
		ArmorAbility[] abilities = hero.heroClass.armorAbilities();
		String[] labels = new String[abilities.length + 1];
		int selected = 0;
		labels[0] = "None";
		for (int i = 0; i < abilities.length; i++) {
			labels[i + 1] = InventoryEditor.capitalize(abilities[i].name());
			if (hero.armorAbility != null && hero.armorAbility.getClass() == abilities[i].getClass()) selected = i + 1;
		}
		addSection("Armor Ability");
		addChoiceRow(labels, selected, index -> TalentEditor.setArmorAbility(hero, index == 0 ? null : abilities[index - 1]));
	}

	interface ChoiceSetter {
		void choose(int index);
	}

	private void addChoiceRow(String[] labels, int selected, ChoiceSetter setter) {
		addChoiceRow(labels, selected, labels.length <= 3 ? labels.length : 2, setter);
	}

	private void addChoiceRow(String[] labels, int selected, int perRow, ChoiceSetter setter) {
		float width = (WIDTH - (perRow - 1) * GAP) / (float) perRow;
		for (int i = 0; i < labels.length; i++) {
			int index = i;
			int column = i % perRow;
			if (i > 0 && column == 0) pos += ROW_HEIGHT + GAP;
			RedButton button = new RedButton(labels[i], 6) {
				@Override
				public void onClick() {
					if (index == selected) return;
					setter.choose(index);
					refresh();
				}
			};
			if (i == selected) button.textColor(TITLE_COLOR);
			button.setRect(column * (width + GAP), pos, width, ROW_HEIGHT);
			add(button);
		}
		pos += ROW_HEIGHT + GAP;
	}

	private void addTalentRow(Hero hero, Talent talent) {
		int points = hero.pointsInTalent(talent);
		int buttons = TalentEditor.canSwap(tier) ? 3 : 2;
		float countWidth = 14;
		float nameWidth = WIDTH - buttons * (STEP_WIDTH + GAP) - countWidth - GAP;
		float rowHeight = TALENT_ROW_HEIGHT;

		RedButton name = new RedButton("") {
			@Override
			public void onClick() {
				showInfo(talent, points);
			}

			@Override
			public boolean onLongClick() {
				showInfo(talent, points);
				return true;
			}
		};
		name.setRect(0, pos, nameWidth, rowHeight);
		add(name);

		TalentIcon icon = new TalentIcon(talent);
		icon.x = 3;
		icon.y = pos + (rowHeight - icon.height()) / 2f;
		add(icon);

		RenderedTextBlock label = PixelScene.renderTextBlock(talent.title(), 6);
		label.maxWidth((int) (nameWidth - icon.width() - 6));
		label.setPos(icon.width() + 5, pos + (rowHeight - label.height()) / 2f);
		add(label);

		RenderedTextBlock count = PixelScene.renderTextBlock(points + "/" + talent.maxPoints(), 6);
		if (points > 0) count.hardlight(TITLE_COLOR);
		count.setPos(name.right() + GAP + (countWidth - count.width()) / 2f, pos + (rowHeight - count.height()) / 2f);
		add(count);

		float x = name.right() + GAP + countWidth + GAP;
		x = addSmallButton("-", x, rowHeight, () -> TalentEditor.removePoint(hero, tier, talent));
		x = addSmallButton("+", x, rowHeight, () -> TalentEditor.addPoint(hero, tier, talent));
		if (TalentEditor.canSwap(tier)) {
			RedButton swap = new RedButton("") {
				@Override
				public void onClick() {
					hide();
					Game.scene().addToFront(new TalentSwapWindow(talent, tier, null));
				}
			};
			swap.icon(Icons.get(Icons.SHUFFLE));
			swap.setRect(x, pos, STEP_WIDTH, rowHeight);
			add(swap);
		}
		pos += rowHeight + GAP;
	}

	private float addSmallButton(String label, float x, float height, Runnable action) {
		RedButton button = new RedButton(label) {
			@Override
			public void onClick() {
				action.run();
				refresh();
			}
		};
		button.setRect(x, pos, STEP_WIDTH, height);
		add(button);
		return x + STEP_WIDTH + GAP;
	}

	private void buildModifiers(Hero hero) {
		for (HeroExtras.Mod mod : HeroExtras.Mod.values()) {
			int value = HeroExtras.mod(mod);
			String text = (value >= 0 ? "+" : "") + value + (mod.percent ? "%" : "");
			addStepper(mod.label, text, value, mod.min(), mod.max(), newValue -> HeroExtras.mod(mod, newValue), mod.step());
		}

		RedButton reset = new RedButton("Reset All", 7) {
			@Override
			public void onClick() {
				HeroExtras.resetMods();
				refresh();
			}
		};
		reset.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(reset);
		pos += ROW_HEIGHT + GAP;
	}

	private void buildStatus(Hero hero) {
		List<Buff> active = StatusEditor.active(hero);
		RedButton addStatus = new RedButton("Add Status", 6) {
			@Override
			public void onClick() {
				hide();
				StatusPickerWindow.openFor(Dungeon.hero, () -> open(TAB_STATUS, tier, page));
			}
		};
		addSectionWithButton("Active effects (" + active.size() + ")", addStatus, 50);
		if (active.isEmpty()) addHint("No active effects.");

		int perPage = PixelScene.landscape() ? 3 : 6;
		int pages = Math.max(1, (active.size() + perPage - 1) / perPage);
		int current = Math.min(page, pages - 1);
		int end = Math.min(active.size(), (current + 1) * perPage);
		for (int i = current * perPage; i < end; i++) addStatusRow(active.get(i));
		if (pages > 1) {
			pos += (perPage - (end - current * perPage)) * (ROW_HEIGHT + GAP);
			addPager(current, pages, this::reopenPage);
		}

		RedButton removeNegative = new RedButton("Remove Negative", 6) {
			@Override
			public void onClick() {
				StatusEditor.removeNegative(hero);
				refresh();
			}
		};
		removeNegative.setRect(0, pos, WIDTH, ROW_HEIGHT);
		add(removeNegative);
		pos += ROW_HEIGHT + GAP;

		pos += GAP;
		if (EditorSwitches.hunger()) {
			float top = pos;
			pos = HungerWindow.addSummary(this, 0, pos, WIDTH) + 2 * GAP;
			PointerArea area = new PointerArea(0, top, WIDTH, pos - top) {
				@Override
				public void onClick(PointerEvent event) {
					hide();
					HungerWindow.open(() -> open(TAB_STATUS, tier, page));
				}
			};
			add(area);
		} else {
			RenderedTextBlock title = PixelScene.renderTextBlock("Hunger", 7);
			title.hardlight(TITLE_COLOR);
			title.setPos(0, pos);
			add(title);
			pos = title.bottom() + 2 * GAP;
		}
		addChoiceRow(StatusEditor.HUNGER_STATES, StatusEditor.hungerState(hero), StatusEditor.HUNGER_STATES.length, index -> StatusEditor.setHungerState(hero, index));
	}

	private void addSectionWithButton(String text, RedButton button, float buttonWidth) {
		pos += GAP;
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 7);
		block.hardlight(TITLE_COLOR);
		float rowTop = pos;
		block.setPos(0, rowTop + (ROW_HEIGHT - block.height()) / 2f);
		add(block);
		button.setRect(WIDTH - buttonWidth, rowTop, buttonWidth, ROW_HEIGHT);
		add(button);
		pos = rowTop + ROW_HEIGHT + GAP;
	}

	private void addStatusRow(Buff buff) {
		float smallWidth = 16;
		float turnsWidth = 22;
		boolean infinite = InfiniteStatuses.isInfinite(buff.getClass());
		boolean editable = StatusEditor.isTurnsEditable(buff);
		boolean showTurns = editable || infinite;
		float nameWidth = WIDTH - 3 * (smallWidth + GAP) - (showTurns ? turnsWidth + GAP : 0);

		RedButton name = new RedButton("") {
			@Override
			public void onClick() {
				showBuffInfo(buff);
			}

			@Override
			public boolean onLongClick() {
				showBuffInfo(buff);
				return true;
			}
		};
		name.setRect(0, pos, nameWidth, ROW_HEIGHT);
		add(name);

		BuffIcon icon = new BuffIcon(buff, true);
		icon.x = 3;
		icon.y = pos + (ROW_HEIGHT - icon.height()) / 2f;
		add(icon);

		String turns = StatusEditor.turnsOf(buff);
		String text = StatusEditor.nameOf(buff) + (showTurns || turns.isEmpty() ? "" : " (" + turns + ")");
		RenderedTextBlock label = PixelScene.renderTextBlock(text, 6);
		label.maxWidth((int) (nameWidth - icon.width() - 6));
		if (buff.type == Buff.buffType.POSITIVE) label.hardlight(POSITIVE_COLOR);
		if (buff.type == Buff.buffType.NEGATIVE) label.hardlight(NEGATIVE_COLOR);
		label.setPos(icon.width() + 5, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);

		float x = name.right() + GAP;
		if (showTurns) {
			int current = StatusEditor.turnsValue(buff);
			RedButton turnsButton = new RedButton(infinite ? "-" : String.valueOf(current), 6) {
				@Override
				public void onClick() {
					if (infinite) return;
					promptValue("Turns left", current, 1, 9999, value -> StatusEditor.setTurns(buff, value));
				}
			};
			turnsButton.setRect(x, pos, turnsWidth, ROW_HEIGHT);
			turnsButton.enable(!infinite);
			add(turnsButton);
			x += turnsWidth + GAP;
		}

		RedButton infiniteButton = new RedButton("") {
			@Override
			public void onClick() {
				InfiniteStatuses.setInfinite(buff, !infinite);
				refresh();
			}
		};
		Image star = Icons.get(Icons.TALENT);
		if (!infinite) star.alpha(0.3f);
		infiniteButton.icon(star);
		infiniteButton.setRect(x, pos, smallWidth, ROW_HEIGHT);
		add(infiniteButton);
		x += smallWidth + GAP;

		IconButton info = new IconButton(Icons.get(Icons.INFO)) {
			@Override
			public void onClick() {
				showBuffInfo(buff);
			}
		};
		info.setRect(x, pos, smallWidth, ROW_HEIGHT);
		add(info);
		x += smallWidth + GAP;

		RedButton remove = new RedButton("X", 7) {
			@Override
			public void onClick() {
				InfiniteStatuses.removeForever(buff);
				refresh();
			}
		};
		remove.setRect(x, pos, smallWidth, ROW_HEIGHT);
		add(remove);
		pos += ROW_HEIGHT + GAP;
	}

	interface PageOpener {
		void open(int page);
	}

	void addPager(int current, int pages, PageOpener opener) {
		RedButton previous = new RedButton("", 7) {
			@Override
			public void onClick() {
				opener.open((current + pages - 1) % pages);
			}
		};
		previous.icon(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.LEFTARROW));
		previous.setRect(0, pos, 24, ROW_HEIGHT);
		add(previous);

		RenderedTextBlock label = PixelScene.renderTextBlock("Page " + (current + 1) + " / " + pages, 6);
		label.setPos((WIDTH - label.width()) / 2f, pos + (ROW_HEIGHT - label.height()) / 2f);
		add(label);

		RedButton next = new RedButton("", 7) {
			@Override
			public void onClick() {
				opener.open((current + 1) % pages);
			}
		};
		next.icon(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.get(com.shatteredpixel.shatteredpixeldungeon.ui.Icons.RIGHTARROW));
		next.setRect(WIDTH - 24, pos, 24, ROW_HEIGHT);
		add(next);
		pos += ROW_HEIGHT + GAP;
	}

	static void showBuffInfo(Buff buff) {
		try {
			Game.scene().addToFront(new WndInfoBuff(buff));
		} catch (Throwable ignored) {
		}
	}

	private void addStepper(String label, String display, int current, int min, int max, IntSetter setter) {
		addStepper(label, display, current, min, max, setter, 1);
	}

	private void addStepper(String label, String display, int current, int min, int max, IntSetter setter, int step) {
		RenderedTextBlock text = PixelScene.renderTextBlock(label, 7);
		text.setPos(0, pos + (ROW_HEIGHT - text.height()) / 2f);
		add(text);

		float x = WIDTH - 2 * STEP_WIDTH - VALUE_WIDTH - 2 * GAP;
		add(stepButton("-", x, current, -step, setter));

		RedButton value = new RedButton(display, 6) {
			@Override
			public void onClick() {
				promptValue(label, current, min, max, setter);
			}
		};
		value.setRect(x + STEP_WIDTH + GAP, pos, VALUE_WIDTH, ROW_HEIGHT);
		add(value);

		add(stepButton("+", value.right() + GAP, current, step, setter));
		pos += ROW_HEIGHT + GAP;
	}

	private RedButton stepButton(String label, float x, int current, int delta, IntSetter setter) {
		RedButton button = new RedButton(label) {
			@Override
			public void onClick() {
				setter.set(current + delta);
				refresh();
			}

			@Override
			public boolean onLongClick() {
				setter.set(current + delta * 10);
				refresh();
				return true;
			}
		};
		button.setRect(x, pos, STEP_WIDTH, ROW_HEIGHT);
		return button;
	}

	private void promptValue(String label, int current, int min, int max, IntSetter setter) {
		NumberPrompt.show(label, current, min, max, setter::set, this::refresh);
	}

	private void addSection(String text) {
		pos += GAP;
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 7);
		block.hardlight(TITLE_COLOR);
		block.setPos(0, pos);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	private void addStat(String label, String value) {
		RenderedTextBlock left = PixelScene.renderTextBlock(label, 6);
		left.setPos(0, pos);
		add(left);
		RenderedTextBlock right = PixelScene.renderTextBlock(value, 6);
		right.setPos(WIDTH - right.width(), pos);
		add(right);
		pos = Math.max(left.bottom(), right.bottom()) + GAP;
	}

	private void addHint(String text) {
		RenderedTextBlock block = PixelScene.renderTextBlock(text, 6);
		block.maxWidth(WIDTH);
		block.hardlight(DIM_COLOR);
		block.setPos(0, pos + GAP);
		add(block);
		pos = block.bottom() + 2 * GAP;
	}

	private static void showInfo(Talent talent, int points) {
		Game.scene().addToFront(new WndInfoTalent(talent, points, null));
	}

	private static String lockedReason(int tier) {
		if (tier == 3) return "Tier 3 unlocks at level " + (Talent.tierLevelThresholds[3] - 1) + " once you choose a subclass.";
		if (tier == 4) return "Tier 4 unlocks at level " + (Talent.tierLevelThresholds[4] - 1) + " once you choose an armor ability.";
		return "Tier " + tier + " unlocks at level " + (Talent.tierLevelThresholds[tier] - 1) + ".";
	}

	private static String modSuffix(HeroExtras.Mod mod) {
		int value = HeroExtras.mod(mod);
		if (value == 0) return "";
		return " _(" + (value > 0 ? "+" : "") + value + (mod.percent ? "%" : "") + ")_";
	}

	private static String damageRange(Hero hero) {
		KindOfWeapon weapon = hero.belongings.weapon();
		if (weapon == null) return "unarmed";
		int bonus = HeroExtras.mod(HeroExtras.Mod.DAMAGE);
		return Math.max(0, weapon.min() + bonus) + " - " + Math.max(0, weapon.max() + bonus);
	}

	private static String armorRange(Hero hero) {
		Armor armor = hero.belongings.armor();
		int bonus = HeroExtras.mod(HeroExtras.Mod.ARMOR);
		int min = armor == null ? 0 : armor.DRMin();
		int max = armor == null ? 0 : armor.DRMax();
		return Math.max(0, min + bonus) + " - " + Math.max(0, max + bonus);
	}

	private static String format(float value) {
		return String.format(Locale.ENGLISH, "%.2f", value);
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private interface StatSource {
		String get();
	}

	private static String safe(StatSource source) {
		try {
			return source.get();
		} catch (Exception e) {
			return "?";
		}
	}
}
