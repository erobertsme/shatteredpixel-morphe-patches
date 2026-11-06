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
> **[v1.0.0-dev.54](https://github.com/erobertsme/shatteredpixel-morphe-patches/releases/tag/v1.0.0-dev.54)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;10 patches total
<details open>
<summary>📦 Shattered Pixel Dungeon&nbsp;&nbsp;•&nbsp;&nbsp;10 patches</summary>
<br>

**Package name:** `com.shatteredpixel.shatteredpixeldungeon`

**🎯 Supported versions:**

| 4.0.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Character editor](#character-editor) | Adds a Character button to the in-game menu for editing health, level, experience, strength, talents and status effects, and for adding modifiers to accuracy, evasion, damage, armor, speed and regeneration. Changes are saved with the run. |  |
| [Enemy editor](#enemy-editor) | Adds an Edit button when you examine an enemy, NPC or other creature, for editing its health, side, behavior and status effects. |  |
| [Floor editor](#floor-editor) | Adds a Floor button to the in-game menu and an Edit tile button when you examine a tile or trap, for removing or adding gases and other area effects and for adding, removing, hiding, revealing and disarming traps. Examining an item on the floor lets you move it to another tile. Examining a tile lists everything on it, including every item in a pile and the tile itself. |  |
| [Hunger editor](#hunger-editor) | Adds a Hunger window for changing how long each hunger stage lasts, how often and how hard starvation hurts, or disabling hunger. Main menu settings are the default for every game; changes made during a game are saved to that game. |  |
| [Inventory editor](#inventory-editor) | Adds an Inventory button to the in-game menu for editing gold and energy, adding any item in the game, and opening the backpack with an Edit button on every item (level, quantity, curses, enchantments, glyphs, augments and charges). Also adds optional unlimited inventory space with pages, and Weapons, Armor and Rings backpack tabs that hold those items like the potion bandolier. |  |
| [QoL enhancements](#qol-enhancements) | Quality of life options in the Extras menu. Confirm actions: a tap that would attack, walk somewhere, use stairs or step into danger first shows what will happen, with Go and Cancel buttons. Shopkeeper: banish enemies from the shop, keep the shopkeeper from fleeing fights, area effects or attacks, a lenient three strikes rule, and how many sold items can be bought back. Quantum Chest: a chest beside the exit of each beaten boss floor, all sharing the same storage. |  |
| [Save management](#save-management) | Export and import all saves as one zip file from the main menu Extras and the app icon shortcuts, and keep up to 10 saves of each game to load back later. Options: a public save folder, uncompressed saves and save file access. | • Public save folder<br>• Save folder<br>• Uncompressed saves<br>• Save file access |
| [Supporter options](#supporter-options) | NOTICE: This patch is not intended to gain these bonus extras for free. It is meant to ensure supporters are able to keep the benefits they paid for. Don't be a scumbag. Please support the developer!<br><br>Adds Supporter options to the Extras menu: keep your Silver, Golden or Shattered supporter rewards, and hide the Support button on the title screen. Only works on the Google Play version of the game. |  |
| [Teleport](#teleport) | Teleport features: a Teleport to... button in the Adventuring Notes for travelling to any floor, Teleport Here for the hero in the Floor editor, and Teleport for creatures in the Enemy and NPC editor. |  |
| [Unlock all](#unlock-all) | Adds toggles to the Extras menu that unlock all heroes, guide books, intros and lore, the item catalog and the bestiary. Your save data is not changed, so turning a toggle off restores your real progress. |  |

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
