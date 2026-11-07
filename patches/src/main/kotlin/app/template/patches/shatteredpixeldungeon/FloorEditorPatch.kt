package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val FLOOR_CLASS = "$EXTENSION_PACKAGE/FloorEditor;"
private const val AFTER_INFO = "invoke-static/range { p0 .. p0 }, $FLOOR_CLASS->afterInfo(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V"

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

		WndInfoCellConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $FLOOR_CLASS->beforeCellInfo(I)V")
		WndInfoCellConstructorFingerprint.callBeforeReturn(AFTER_INFO)
		WndInfoTrapConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $FLOOR_CLASS->beforeTrapInfo(Ljava/lang/Object;)V")
		WndInfoTrapConstructorFingerprint.callBeforeReturn(AFTER_INFO)
		WndInfoHeapConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $FLOOR_CLASS->beforeHeapInfo(Ljava/lang/Object;)V")
		WndInfoHeapConstructorFingerprint.callBeforeEveryReturn(AFTER_INFO)

	}
}
