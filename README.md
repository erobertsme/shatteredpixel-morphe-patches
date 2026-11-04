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
> **[v1.0.0-dev.10](https://github.com/erobertsme/shatteredpixel-morphe-patches/releases/tag/v1.0.0-dev.10)**&nbsp;&nbsp;•&nbsp;&nbsp;`dev`&nbsp;&nbsp;•&nbsp;&nbsp;7 patches total
<details open>
<summary>📦 Shattered Pixel Dungeon&nbsp;&nbsp;•&nbsp;&nbsp;7 patches</summary>
<br>

**Package name:** `com.shatteredpixel.shatteredpixeldungeon`

**🎯 Supported versions:**

| 4.0.2 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Character editor](#character-editor) | Adds a Character button to the in-game menu for editing health, level, experience, strength, talents and status effects, and for adding modifiers to accuracy, evasion, damage, armor, speed and regeneration. Changes are saved with the run. |  |
| [Inventory editor](#inventory-editor) | Adds an Inventory button to the in-game menu for editing gold, energy and items (level, quantity, curses, enchantments, glyphs, augments and charges) and for adding any item in the game. |  |
| [Public save folder](#public-save-folder) | Stores save files in a public folder on the device so they can be opened with any file manager. Asks for storage access on first launch and copies existing saves over. | • Save folder |
| [Save backup](#save-backup) | Adds Export saves and Import saves to the Extras menu and to the app icon shortcuts (long-press it) for backing up and restoring all saves as a zip file. |  |
| [Save file access](#save-file-access) | Exposes the game's save folder in the system file picker and Files app so saves can be copied, backed up, and edited. |  |
| [Uncompressed saves](#uncompressed-saves) | Writes save files as plain JSON instead of GZIP so they can be edited in any text editor. Existing compressed saves still load and are converted the next time the game saves. |  |
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
