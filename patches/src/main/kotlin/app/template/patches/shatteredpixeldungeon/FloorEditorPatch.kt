package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.util.smali.ExternalLabel
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val FLOOR_CLASS = "$EXTENSION_PACKAGE/FloorEditor;"
private const val TILE_CONTENTS_CLASS = "$EXTENSION_PACKAGE/TileContents;"
private const val AFTER_INFO = "invoke-static/range { p0 .. p0 }, $FLOOR_CLASS->afterInfo(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V"

@Suppress("unused")
val floorEditorPatch = bytecodePatch(
	name = "Floor editor",
	description = "Adds a Floor button to the in-game menu and an Edit tile button when you examine a tile or trap, for removing or adding gases and other area effects and for adding, removing, hiding, revealing and disarming traps. Examining an item on the floor lets you move it to another tile. Examining a tile lists everything on it, including every item in a pile and the tile itself.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		FloorEditorAvailableFingerprint.returnTrue()

		WndInfoCellConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $FLOOR_CLASS->beforeCellInfo(I)V")
		WndInfoCellConstructorFingerprint.callBeforeReturn(AFTER_INFO)
		WndInfoTrapConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $FLOOR_CLASS->beforeTrapInfo(Ljava/lang/Object;)V")
		WndInfoTrapConstructorFingerprint.callBeforeReturn(AFTER_INFO)
		WndInfoHeapConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $FLOOR_CLASS->beforeHeapInfo(Ljava/lang/Object;)V")
		WndInfoHeapConstructorFingerprint.callBeforeEveryReturn(AFTER_INFO)

		ExamineCellFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0 }, $TILE_CONTENTS_CLASS->examine(Ljava/lang/Integer;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)
	}
}
