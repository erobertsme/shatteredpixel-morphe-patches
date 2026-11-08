package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val TELEPORT_CLASS = "$EXTENSION_PACKAGE/Teleport;"
private const val TELEPORT_FLOOR_CLASS = "$EXTENSION_PACKAGE/FloorEditor;"
private const val TELEPORT_AFTER_INFO = "invoke-static/range { p0 .. p0 }, $TELEPORT_FLOOR_CLASS->afterInfo(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V"

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

		WndInfoCellConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $TELEPORT_FLOOR_CLASS->beforeCellInfo(I)V")
		WndInfoCellConstructorFingerprint.callBeforeReturn(TELEPORT_AFTER_INFO)
		WndInfoTrapConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $TELEPORT_FLOOR_CLASS->beforeTrapInfo(Ljava/lang/Object;)V")
		WndInfoTrapConstructorFingerprint.callBeforeReturn(TELEPORT_AFTER_INFO)
		WndInfoHeapConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $TELEPORT_FLOOR_CLASS->beforeHeapInfo(Ljava/lang/Object;)V")
		WndInfoHeapConstructorFingerprint.callBeforeEveryReturn(TELEPORT_AFTER_INFO)

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
