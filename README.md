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
> **[v1.0.0-dev.85](https://github.com/erobertsme/shatteredpixel-morphe-patches/releases/tag/v1.0.0-dev.85)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;10 patches total
<details open>
<summary>📦 Shattered Pixel Dungeon&nbsp;&nbsp;•&nbsp;&nbsp;10 patches</summary>
<br>

**Package name:** `com.shatteredpixel.shatteredpixeldungeon`

**🎯 Supported versions:**

| 4.0.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Character editor](#character-editor) | Edit your hero's stats, talents, subclass, armor ability, modifiers and status effects. |  |
| [Enemy editor](#enemy-editor) | Edit any creature's health, side, behavior and status effects. |  |
| [Floor editor](#floor-editor) | Edit the tiles, traps, plants, area effects, items and creatures on the current floor, and draw the floor by dragging. |  |
| [Hunger editor](#hunger-editor) | Change how long each hunger stage lasts and how starving hurts, or switch hunger off. |  |
| [Inventory editor](#inventory-editor) | Edit gold, energy and every item, add any item, and identify or uncurse everything. Adds unlimited inventory space and Weapons, Armor and Rings backpack tabs. |  |
| [QoL enhancements](#qol-enhancements) | Confirm risky taps, a calmer shopkeeper, a Quantum Chest on boss floors, bigger bags, extra toolbar item slots, an Attack Tile button, an Enhanced Bestiary, a hero stats button, experience and loot rules and more items to energize. |  |
| [Save management](#save-management) | Export and import all saves as a zip, and keep up to 10 saves of each game. Optional public, uncompressed save files. | • Public save folder<br>• Save folder<br>• Uncompressed saves<br>• Save file access |
| [Supporter options](#supporter-options) | NOTICE: This patch is not intended to gain these bonus extras for free. It is meant to ensure supporters are able to keep the benefits they paid for. Don't be a scumbag. Please support the developer!<br><br>Keep your supporter rewards and hide the Support button. Google Play version only. |  |
| [Teleport](#teleport) | Teleport to any floor, and teleport your hero or other creatures to any tile. |  |
| [Unlock all](#unlock-all) | Show all heroes, guides, lore, catalog items and bestiary entries as unlocked without changing your save. |  |

</details>

<!-- PATCHES_END -->

## 📖 Patch details

### Extras menu
Every patch that adds settings puts them in **Extras**, on the title screen and in the in-game menu. Extras has General, QoL, Unlocks and Editors tabs, and each tab only appears when its patch is included. The [i] buttons throughout explain each window, split into pages with icons where it helps.

### Character editor
- **Character** button in the in-game menu, shown with your hero's portrait, and an **Edit** button on each tab of the hero info window. A status's info window, opened from the status icons or the hero info window, has a **Remove** button in its top right corner.
- **Stats:** health, max health, level, experience, strength and shield.
- **Talents:** add or remove points and bonus points per tier, swap any talent like a Scroll of Metamorphosis (with an [i] for each choice), and change your **subclass** (bottom of tier 3) or **armor ability** (bottom of tier 4).
- **Mods:** add or subtract modifiers to accuracy, evasion, damage, armor, speed and regeneration.
- **Status:** edit the turns left on any status, make one infinite (the star), remove one or all negative ones, and add any status from a picker with favorites.
- Everything is saved with the run.

### Enemy editor
- Small **Edit** button in the top right corner when you examine an enemy, NPC or other creature. Its status info windows have a **Remove** button.
- Edit its health and max health, switch it between enemy, neutral and ally, and set what it's doing (sleeping, wandering, hunting, fleeing or passive).
- **Move to Tile** sends it walking to a tile you tap; **Defeat** kills it.
- Edit, add or remove its status effects.
- With the Teleport patch, **Teleport** moves it straight to a tile you tap.

### Floor editor
- **Floor** button in the in-game menu, and a small **Edit** button in the top right corner when you examine a tile, trap or item on the floor.
- **Tile:** add or remove gases and other area effects; add, change, remove, hide, reveal, disarm and trigger traps; plant seeds or remove plants; change the terrain, open, close, lock, unlock or hide doors; spawn any creature, at a chosen level for creatures that scale; place any item, choosing whether a trap there stays armed; delete the tile to an empty pit.
- **Quick buttons** on examine windows: doors (Open, Close, Lock, Unlock), traps (Activate, Deactivate, Hidden, Visible), grass (Unfurrowed, Furrowed, Trampled), exits (Lock, Unlock), chasms (Fill In) and barricades (Break). Hidden and Visible only show for traps that can be hidden.
- **Items on the floor:** Move Item or Move Pile to another tile, with the destination highlighted and a choice about any trap there.
- **Whole floor:** list every gas and area effect, including a Wand of Regrowth Lotus with its range and turns left (its falling leaves cover every tile in range), and remove them one at a time or all at once; remove all traps, reveal, hide, disarm or re-arm all traps, reveal or forget the map, defeat all enemies, allies or neutral creatures.
- **Draw:** drag across the map to paint floor, wall or empty pit, move the view around, then Finish to keep or Cancel to undo.
- **Edit Floor** mode: the game is saved, then tapping any tile shows its info with an Edit button and a button for each creature, item, plant or trap on it; drag to look around. **Save** keeps the changes, **Cancel** asks "Dismiss all changes and exit edit mode?" and then reloads the game as it was.

### Hunger editor
- **Hunger** window (Extras → General, the character editor, and the hunger summary on the hero info window).
- Change how many turns you stay full and hungry, with 75%, 50% and 30% presets, or switch hunger off.
- Starvation damage follows the game's formula or a fixed amount every so many turns, or can be switched off.
- Main menu settings are the defaults for new games; changes made during a game are saved with that game.

### Inventory editor
- **Inventory** button in the in-game menu.
- Edit gold and energy, add any item from a picker with categories and favorites, identify your carried items, learn every potion, scroll or ring type, and remove every curse.
- **Open Backpack** shows your real backpack in edit mode, titled *Backpack [Editing]*. Tapping an item goes straight to its editor. Outside edit mode, every item's description has an **Edit** button in its header. The editor covers level, quantity, identified, cursed, enchantment or glyph, augment, charges with an **infinite charges** option, waterskin water, blessing an ankh, alchemy conversions, duplicate and delete. A gear button returns to the inventory editor.
- **Unlimited inventory space:** every bag always has room for one more item, shown in pages of 8 rows with arrows that wrap and remember your page.
- **Weapons, Armor and Rings tabs** (switched on in Extras → QoL → Backpack): hold those items like the potion bandolier, so they don't use backpack space.
- Only one of each bag can be carried; extra copies are refused or dropped.

### QoL enhancements
- **Confirm actions:** a tap that would walk somewhere, attack, talk, use stairs or step into danger first shows what will happen, with Go and Cancel. Each action can be switched on or off, and moving and attacking have their own minimum distances. Danger accounts for levitation and immunities.
- **Interrupted path:** when a walk is interrupted and the game shows its resume arrow, the rest of the route and its destination are drawn on the map, the same way Confirm actions shows a path. Can be switched off in Extras → QoL.
- **Enhanced Bestiary:** examining a creature, or opening one in the bestiary, adds a **Stats** button at the bottom (health, accuracy, evasion, damage and armor ranges, attack and move speed, sight, experience, drops, and alignment and state for live creatures) and an **[i]** button in the top right corner listing its special abilities, traits, immunities and resistances, each with an icon. Can be switched off in Extras → QoL.
- **Experience** and **Loot** (separate pages in Extras → QoL): each creature type stops giving experience once your level passes its limit, and stops dropping items 2 levels later. For each, choose Normal, No level limit, Raise the limit (by 1 to 30 levels), Reduced past the limit (5% to 100%), or a Multiplier (10% to 1000%).
- **List everything on a tile:** examining a tile with more than one thing on it, such as a creature on an item or a pile of items, lists each one. Pick an item from a pile to read it, pick it up instead of the top item, or move it with the Floor editor. Items that ended up inside a grave, skeleton or chest after the floor was made, like gold dropped by a creature defeated on a grave, are listed under it. Can be switched off in Extras → QoL.
- **Energize more items:** lets you energize items in alchemy that normally can't be. For now that's the Arcane Stylus, worth 5 energy each. Can be switched off in Extras → QoL.
- **Tap unexplored areas:** tapping a blacked-out part of the map you haven't seen yet walks to the reachable explored tile closest to where you tapped, with the usual movement confirmation if it's on. Can be switched off in Extras → QoL.
- **Hero stats button:** a stats button beside the [i] on the hero info window opens your hero's health, experience, strength, accuracy, evasion, damage, armor, attack and move speed, and shield, each with an icon. Can be switched off in Extras → QoL.
- **Attack Tile button:** a button above the backpack button that lets you attack any tile in reach of your weapon except a chasm, walls and doors included. A creature there is attacked even if it is hiding, so you can hit a mimic first, often as a surprise attack. An empty tile gets a swing that takes the time of an attack. Tap the button again to cancel. With **Enchantments affect the tile** on, a Blazing, Chilling, Blooming or Shocking weapon rolls its normal chance and sets the tile alight (doors, bookshelves and grass burn away), freezes it, grows grass there or sends lightning from it that arcs between creatures, further through water. Both are switches in Extras → QoL → Toolbar.
- **Shopkeeper:**
  - Banish Enemies: enemies that walk into the shop are thrown out and stay away for 20 turns.
  - Fearless: he isn't scared off by enemies fighting in the shop, or by you carrying the Amulet back up.
  - Immune: gas, fire and cold don't drive him away.
  - Passive: your attacks only earn a remark, and thrown weapons go up for sale.
  - Lenient: two warnings, then you're thrown out until you apologize with gold or a potion of healing. Getting caught stealing means paying for the item or leaving.
  - Shop window: buy back up to 24 sold items, 8 per page, with an [i] to examine each.
- **Quantum Chest:** a chest beside the exit of every beaten boss floor. Every chest opens the same storage of 1, 2 or 3 pages of 25 items. Tap an item for its details and Take Out or Store Item, or turn on Quick Move.
- **Backpack** (window in Extras → QoL): 4 to 8 rows of items in every bag, and an optional extra page for each tab.
- **Extra toolbar item slots:** a row of extra quickslots across the full width of the screen, below the toolbar and lined up with the slots above. With the game's Quickslot Swapper setting on, there's no extra row: the swap button appears beside your quickslots and swaps to the extra slots instead.

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
- **Teleport** in the in-game menu and **Teleport to...** in the Adventuring Notes: travel to any floor, arriving at its entrance or exit. Tapping a floor number in the Adventuring Notes asks to teleport there.
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
