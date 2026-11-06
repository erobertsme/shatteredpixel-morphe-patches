# 🍀 Cloverlord Patches

Morphe patches for [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon) 4.0.2, Google Play build.

## ❓ About

Cloverlord Patches add an **Extras** menu to Shattered Pixel Dungeon, with editors for your hero, inventory, enemies, floors and hunger, quality of life options, save management, teleporting and unlocks. Every patch is optional, and the editors can be switched on and off in **Extras → Editors**.

Patch settings work like this: settings changed on the main menu are the defaults for new games, and settings changed during a game are saved with that game. Unlocks, supporter options, favorites and hunger keep their own rules.

The full details of each patch are under [Patch details](#-patch-details).

### How to use these patches

Click the button below, or manually add this repo url to Morphe Manager: https://github.com/erobertsme/shatteredpixel-morphe-patches

<a href="https://morphe.software/add-source?github=erobertsme/shatteredpixel-morphe-patches">
  <img src=".github/assets/add-to-morphe.svg" alt="Add to Morphe" height="48">
</a>

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0-dev.57](https://github.com/erobertsme/shatteredpixel-morphe-patches/releases/tag/v1.0.0-dev.57)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;10 patches total
<details open>
<summary>📦 Shattered Pixel Dungeon&nbsp;&nbsp;•&nbsp;&nbsp;10 patches</summary>
<br>

**Package name:** `com.shatteredpixel.shatteredpixeldungeon`

**🎯 Supported versions:**

| 4.0.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Character editor](#character-editor) | Adds a Character button to the in-game menu, shown with your hero's portrait, and an Edit button on the hero info window. Edit health, max health, level, experience, strength and shield. Add or remove talent points and bonus points, swap talents like a Scroll of Metamorphosis, and change your subclass and armor ability. Add modifiers to accuracy, evasion, damage, armor, speed and regeneration. Add, remove, time or make infinite any status effect. Changes are saved with the run. Can be switched off in Extras, Editors. |  |
| [Enemy editor](#enemy-editor) | Adds an Edit button when you examine an enemy, NPC or other creature. Edit its health, side and behavior, add, remove or time its status effects, send it walking to a tile, or defeat it. With the Teleport patch, teleport it to any tile. Can be switched off in Extras, Editors. |  |
| [Floor editor](#floor-editor) | Adds a Floor button to the in-game menu and an Edit Tile button when you examine a tile, trap or item on the floor. Add or remove gases and other area effects; add, remove, hide, reveal, disarm and trigger traps; plant seeds; change terrain and doors; spawn creatures at a chosen level; place items, choosing whether a trap there stays armed; and delete tiles to an empty pit. For the whole floor: reveal or forget the map, defeat groups of creatures, and draw floors, walls and pits by dragging, with Finish and Cancel. Examine windows get quick buttons for doors, traps, grass, exits, chasms and barricades, and items on the floor can be moved to another tile. Examining a tile lists everything on it, including every item in a pile and the tile itself. Can be switched off in Extras, Editors. |  |
| [Hunger editor](#hunger-editor) | Adds a Hunger window for changing how long each hunger stage lasts, with presets, how often and how hard starvation hurts, or switching hunger off. Main menu settings are the default for every game; changes made during a game are saved to that game. The hunger summary on the hero info window shows the current settings and opens the window when tapped. Can be switched off in Extras, Editors. |  |
| [Inventory editor](#inventory-editor) | Adds an Inventory button to the in-game menu for editing gold and energy, adding any item in the game, identifying carried items, learning every potion, scroll and ring type, and removing every curse. Opens the backpack in edit mode with an Edit button on every item: level, quantity, identified, curses, enchantments and glyphs, augments, charges with an infinite charges option, waterskin water, blessing an ankh, alchemy conversions, duplicating and deleting. Also adds optional unlimited inventory space with pages, Weapons, Armor and Rings backpack tabs that hold those items like the potion bandolier, and keeps you to one bag of each kind. Can be switched off in Extras, Editors. |  |
| [QoL enhancements](#qol-enhancements) | Quality of life options in Extras, QoL. Confirm actions: a tap that would attack, walk somewhere, use stairs or step into danger first shows what will happen, with Go and Cancel buttons and adjustable distances. Shopkeeper: banish enemies from the shop, keep the shopkeeper calm through fights, area effects and your attacks, a lenient three strikes rule with apologies and theft handling, thrown weapons go up for sale, and a shop window that buys back up to 24 sold items. Quantum Chest: a chest beside the exit of every beaten boss floor, all sharing one storage of 25 to 75 items. Backpack: 4 to 8 rows of items in every bag and an optional extra page. Settings changed during a game are saved with that game. |  |
| [Save management](#save-management) | Export and import all saves as one zip file from the main menu Extras and the app icon shortcuts. Game Saves, in Extras and the in-game menu, keeps up to 10 saves of each game to load back later; when a game ends, the title screen asks whether to delete or keep its saves. Options: a public save folder, uncompressed saves and save file access. | • Public save folder<br>• Save folder<br>• Uncompressed saves<br>• Save file access |
| [Supporter options](#supporter-options) | NOTICE: This patch is not intended to gain these bonus extras for free. It is meant to ensure supporters are able to keep the benefits they paid for. Don't be a scumbag. Please support the developer!<br><br>Adds Supporter options to Extras, Unlocks: keep your Silver, Golden or Shattered supporter rewards, and hide the Support button on the title screen. Only works on the Google Play version of the game. |  |
| [Teleport](#teleport) | Adds Teleport to the in-game menu and a Teleport to... button in the Adventuring Notes for travelling to any floor, arriving at its entrance or its exit. Also adds Teleport Here for the hero in the Floor editor and on the tile examine window, and Teleport for creatures in the Enemy and NPC editor. Can be switched off in Extras, Editors. |  |
| [Unlock all](#unlock-all) | Adds toggles to Extras, Unlocks that unlock all heroes, guide books, intros and lore, the item catalog and the bestiary. Your save data is not changed, so turning a toggle off restores your real progress. |  |

</details>

<!-- PATCHES_END -->

## 📖 Patch details

### Extras menu
Every patch that adds settings puts them in **Extras**, on the title screen and in the in-game menu. Extras has General, QoL, Unlocks and Editors tabs, and each tab only appears when its patch is included. The [i] buttons throughout explain each window, split into pages with icons where it helps.

### Character editor
- **Character** button in the in-game menu, shown with your hero's portrait, and an **Edit** button on each tab of the hero info window.
- **Stats:** health, max health, level, experience, strength and shield.
- **Talents:** add or remove points and bonus points per tier, swap any talent like a Scroll of Metamorphosis (with an [i] for each choice), and change your **subclass** (bottom of tier 3) or **armor ability** (bottom of tier 4).
- **Mods:** add or subtract modifiers to accuracy, evasion, damage, armor, speed and regeneration.
- **Status:** edit the turns left on any status, make one infinite (the star), remove one or all negative ones, and add any status from a picker with favorites.
- Everything is saved with the run.

### Enemy editor
- **Edit** button when you examine an enemy, NPC or other creature.
- Edit its health and max health, switch it between enemy, neutral and ally, and set what it's doing (sleeping, wandering, hunting, fleeing or passive).
- **Move to Tile** sends it walking to a tile you tap; **Defeat** kills it.
- Edit, add or remove its status effects.
- With the Teleport patch, **Teleport** moves it straight to a tile you tap.

### Floor editor
- **Floor** button in the in-game menu, and an **Edit Tile** button when you examine a tile, trap or item on the floor.
- **Tile:** add or remove gases and other area effects; add, change, remove, hide, reveal, disarm and trigger traps; plant seeds or remove plants; change the terrain, open, close, lock, unlock or hide doors; spawn any creature, at a chosen level for creatures that scale; place any item, choosing whether a trap there stays armed; delete the tile to an empty pit.
- **Quick buttons** on examine windows: doors (Open, Close, Lock, Unlock), traps (Activate, Deactivate, Hidden, Visible), grass (Unfurrowed, Furrowed, Trampled), exits (Lock, Unlock), chasms (Fill In) and barricades (Break). Hidden and Visible only show for traps that can be hidden.
- **Items on the floor:** Move Item or Move Pile to another tile, with the destination highlighted and a choice about any trap there.
- **Whole floor:** remove all effects or traps, reveal, hide, disarm or re-arm all traps, reveal or forget the map, defeat all enemies, allies or neutral creatures.
- **Draw:** drag across the map to paint floor, wall or empty pit, move the view around, then Finish to keep or Cancel to undo.
- **List everything on a tile** (Extras → Editors): examining a tile lists every creature, every item in a pile, plants, traps and the tile itself. Pick an item from a pile to read it, pick it up instead of the top item, or move it.

### Hunger editor
- **Hunger** window (Extras → General, the character editor, and the hunger summary on the hero info window).
- Change how many turns you stay full and hungry, with 75%, 50% and 30% presets, or switch hunger off.
- Starvation damage follows the game's formula or a fixed amount every so many turns, or can be switched off.
- Main menu settings are the defaults for new games; changes made during a game are saved with that game.

### Inventory editor
- **Inventory** button in the in-game menu.
- Edit gold and energy, add any item from a picker with categories and favorites, identify your carried items, learn every potion, scroll or ring type, and remove every curse.
- **Open Backpack** shows your real backpack in edit mode, with an **Edit** action on every item: level, quantity, identified, cursed, enchantment or glyph, augment, charges with an **infinite charges** option, waterskin water, blessing an ankh, alchemy conversions, duplicate and delete. A gear button returns to the inventory editor.
- **Unlimited inventory space:** every bag always has room for one more item, shown in pages of 8 rows with arrows that wrap and remember your page.
- **Weapons, Armor and Rings tabs** (switched on in Extras → QoL): hold those items like the potion bandolier, so they don't use backpack space.
- Only one of each bag can be carried; extra copies are refused or dropped.

### QoL enhancements
- **Confirm actions:** a tap that would walk somewhere, attack, talk, use stairs or step into danger first shows what will happen, with Go and Cancel. Each action can be switched on or off, and moving and attacking have their own minimum distances. Danger accounts for levitation and immunities.
- **Shopkeeper:**
  - Banish Enemies: enemies that walk into the shop are thrown out and stay away for 20 turns.
  - Fearless: he isn't scared off by enemies fighting in the shop, or by you carrying the Amulet back up.
  - Immune: gas, fire and cold don't drive him away.
  - Passive: your attacks only earn a remark, and thrown weapons go up for sale.
  - Lenient: two warnings, then you're thrown out until you apologize with gold or a potion of healing. Getting caught stealing means paying for the item or leaving.
  - Shop window: buy back up to 24 sold items, 8 per page, with an [i] to examine each.
- **Quantum Chest:** a chest beside the exit of every beaten boss floor. Every chest opens the same storage of 1, 2 or 3 pages of 25 items. Tap an item for its details and Take Out or Store Item, or turn on Quick Move.
- **Backpack:** 4 to 8 rows of items in every bag, and an optional extra page for each tab.
- **Extra quickslot row:** six more quickslots in a row above the toolbar.

### Save management
- **Export saves** and **Import saves** on the main menu Extras and the app icon shortcuts (long-press the icon) back up and restore every save as one zip file.
- **Game Saves** (Extras and the in-game menu): keep up to 10 saves of the game you're playing and load any of them. Each game's saves are kept in their own folder named after its slot and seed. When a game ends, the title screen asks whether to delete or keep its saves, and **Clean Up Saves** removes any you kept.
- Options when patching:
  - **Public save folder:** saves are stored in a folder you can open with any file manager (Documents/ShatteredPixelDungeon by default).
  - **Uncompressed saves:** save files are plain JSON you can edit in any text editor.
  - **Save file access:** shows the game's private save folder in the system file picker and Files app.

### Supporter options
Keep your Silver, Golden or Shattered supporter rewards and hide the Support button on the title screen (Extras → Unlocks). This is meant for supporters keeping what they paid for. Please support the developer. Google Play version only.

### Teleport
- **Teleport** in the in-game menu and **Teleport to...** in the Adventuring Notes: travel to any floor, arriving at its entrance or exit.
- **Teleport Here** for your hero in the floor editor and on tile examine windows, with a warning before landing on an active trap.
- **Teleport** for creatures in the enemy editor.
- Can be switched off in Extras → Editors.

### Unlock all
Show all heroes, guide books, intros and lore, catalog items and bestiary entries as unlocked (Extras → Unlocks). Your save is not changed, so switching a toggle off brings back your real progress.

### Shared
- Unlock toggles, editor switches and other patch settings are stored separately from the game's own settings.
- The patches never add their own item or bag types to your save, so a save still loads if a patch is removed.

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

Cloverlord Patches are licensed under the [GNU General Public License v3.0](LICENSE)
