package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val TELEPORT_CLASS = "$EXTENSION_PACKAGE/Teleport;"

@Suppress("unused")
val teleportPatch = bytecodePatch(
	name = "Teleport",
	description = "Teleport to any floor, and teleport your hero or other creatures to any tile.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		TeleportAvailableFingerprint.returnTrue()

		NotesTabUpdateListFingerprint.callBeforeEveryReturn(
			"invoke-static/range { p0 .. p0 }, $TELEPORT_CLASS->afterNotesLayout(Ljava/lang/Object;)V",
		)
		NotesTabLayoutFingerprint.callBeforeEveryReturn(
			"invoke-static/range { p0 .. p0 }, $TELEPORT_CLASS->afterNotesLayout(Ljava/lang/Object;)V",
		)

		ScrollingGridPaneClickFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p1, p2 }, $TELEPORT_CLASS->onGridClick(Ljava/lang/Object;FF)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)
	}
}
