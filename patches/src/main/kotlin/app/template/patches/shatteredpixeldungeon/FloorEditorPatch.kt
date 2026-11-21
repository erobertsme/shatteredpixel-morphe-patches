package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

@Suppress("unused")
val floorEditorPatch = bytecodePatch(
	name = "Floor editor",
	description = "Edit the tiles, traps, plants, area effects, items and creatures on the current floor, and draw the floor by dragging.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		FloorEditorAvailableFingerprint.returnTrue()

		hookInfoWindows()
	}
}
