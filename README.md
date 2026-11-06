# 🍀 Cloverlord Patches

Template repository for Morphe Patches.

## ❓ About

Patches for apps I like.

<!-- TODO: Update this about section with a brief introduction/summary about this repo and what it offers. -->

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

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

Cloverlord Patches are licensed under the [GNU General Public License v3.0](LICENSE)
