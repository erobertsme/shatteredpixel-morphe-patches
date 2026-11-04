package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

@Suppress("unused")
val uncompressedSavesPatch = bytecodePatch(
	name = "Uncompressed saves",
	description = "Writes save files as plain JSON instead of GZIP so they can be edited in any text editor. Existing compressed saves still load and are converted the next time the game saves.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	execute {
		BundleWriteFingerprint.method.addInstruction(0, "const/4 p2, 0x0")
	}
}
