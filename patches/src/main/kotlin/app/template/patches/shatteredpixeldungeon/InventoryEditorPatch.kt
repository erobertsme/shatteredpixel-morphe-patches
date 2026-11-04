package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

@Suppress("unused")
val inventoryEditorPatch = bytecodePatch(
	name = "Inventory editor",
	description = "Adds an Inventory button to the in-game menu for editing gold, energy and items (level, quantity, curses, enchantments, glyphs, augments and charges) and for adding any item in the game.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		InventoryEditorAvailableFingerprint.returnTrue()
	}
}
