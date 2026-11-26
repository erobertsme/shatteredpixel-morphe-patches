package app.template.extension.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.StartScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.noosa.Game;

/**
 * A game in progress picked from the main menu, shown the way Game Saves are, with its deepest floor and seed, and Continue and Erase.
 */
public class GameSlotWindow extends PreviewWindow {

	private static final int GAME_BUTTON_HEIGHT = 20;

	private final int slot;
	private final GamesInProgress.Info info;
	private final GameSaves.Save save;

	GameSlotWindow(int slot, GamesInProgress.Info info, GameSaves.Save save) {
		super();
		this.slot = slot;
		this.info = info;
		this.save = save;
		layout(InventoryEditor.capitalize(info.heroClass.title()) + " Lv " + info.level + "\n" + save.date(), save);
	}

	@Override
	protected boolean showsLog() {
		return false;
	}

	@Override
	protected String[] extraStats() {
		String deepest = "Deepest " + info.maxDepth;
		return GameSlots.seedCode(info) == null ? new String[] { deepest, "Daily seed" } : new String[] { deepest };
	}

	@Override
	protected StatLink statLink() {
		String seed = GameSlots.seedCode(info);
		return seed == null ? null : new StatLink("Seed", seed, () -> seedChoices(seed));
	}

	/**
	 * Continue and Erase, drawn and placed like the game's own: side by side, 20 pixels tall, with their icons.
	 */
	@Override
	protected void addActions() {
		float half = WIDTH / 2f;
		RedButton continueButton = new RedButton("Continue") {
			@Override
			public void onClick() {
				super.onClick();
				continueGame();
			}
		};
		continueButton.icon(Icons.get(Icons.ENTER));
		continueButton.setRect(0, pos, half - 1, GAME_BUTTON_HEIGHT);
		add(continueButton);
		RedButton eraseButton = new RedButton("Erase") {
			@Override
			public void onClick() {
				super.onClick();
				erase();
			}
		};
		eraseButton.icon(Icons.get(Icons.CLOSE));
		eraseButton.setRect(half + 1, pos, half - 1, GAME_BUTTON_HEIGHT);
		add(eraseButton);
		pos += GAME_BUTTON_HEIGHT + 1;
	}

	private void seedChoices(String seed) {
		ConfirmPrompt.ask("Seed", seed, new String[] { "Copy", "Start New" }, new Runnable[] {
			() -> show(new WndMessage(SaveLocation.copyText("Seed", seed) ? "Copied " + seed + "." : "The seed couldn't be copied.")),
			() -> ConfirmPrompt.ask("Start New", "Start a new game on seed " + seed + "? You'll pick your hero next. This game is kept.", "Start New", () -> newGame(seed)),
		}, "Back");
	}

	private void newGame(String seed) {
		if (!GameSlots.newGameFromSeed(seed)) show(new WndMessage("Every game slot is in use. Erase a game to make room for a new one."));
	}

	private void continueGame() {
		hide();
		GamesInProgress.curSlot = slot;
		Dungeon.hero = null;
		Dungeon.daily = false;
		Dungeon.dailyReplay = false;
		ActionIndicator.clearAction();
		InterlevelScene.mode = InterlevelScene.Mode.CONTINUE;
		Game.switchScene(InterlevelScene.class);
	}

	private void erase() {
		ConfirmPrompt.ask("Erase", "Erase this game? This can't be undone.\n\n" + save.title(), "Erase", () -> {
			hide();
			Dungeon.deleteGame(slot, true);
			ShatteredPixelDungeon.switchNoFade(StartScene.class);
		});
	}

	@Override
	protected void reopen() {
		show(new GameSlotWindow(slot, info, save));
	}
}
