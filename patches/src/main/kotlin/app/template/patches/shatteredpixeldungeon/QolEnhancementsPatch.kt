package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference

private const val PREVIEW_CLASS = "$EXTENSION_PACKAGE/PathPreview;"
private const val SHOPKEEPER_CLASS = "$EXTENSION_PACKAGE/FearlessShopkeeper;"
private const val TILE_CONTENTS_CLASS = "$EXTENSION_PACKAGE/TileContents;"
private const val INVENTORY_SPACE_CLASS = "$EXTENSION_PACKAGE/InventorySpace;"
private const val WND_BAG_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag;"

@Suppress("unused")
val qolEnhancementsPatch = bytecodePatch(
	name = "QoL enhancements",
	description = "Quality of life options in the Extras menu. Confirm actions: a tap that would attack, walk somewhere, use stairs or step into danger first shows what will happen, with Go and Cancel buttons. Fearless shopkeeper: the shopkeeper never runs off with the shop. List everything on a tile: examining a tile lists every item in a pile and the tile itself. Unlimited inventory space.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		PathPreviewAvailableFingerprint.returnTrue()

		DefaultCellListenerFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p1 }, $PREVIEW_CLASS->interceptTap(Ljava/lang/Integer;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

		ShopkeeperFleeFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { }, $SHOPKEEPER_CLASS->stays()Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

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

		BAG_CAPACITY_CLASSES.forEach { className ->
			BagCapacityFingerprint(className).adjustReturns(
				"$INVENTORY_SPACE_CLASS->capacity(I$BAG_CLASS)I",
				"p0",
			)
		}
		BackpackCapacityFingerprint.adjustReturns(
			"$INVENTORY_SPACE_CLASS->backpackCapacity(I$BAG_CLASS)I",
			"p0",
		)

		WndBagConstructorFingerprint.method.apply {
			val instructions = implementation!!.instructions.toList()
			val index = instructions.indexOfFirst {
				it.opcode == Opcode.IPUT && ((it as ReferenceInstruction).reference as FieldReference).name == "nRows"
			}
			if (index < 0) throw PatchException("WndBag does not set nRows")
			val rows = (instructions[index] as TwoRegisterInstruction).registerA
			if (rows > 15) throw PatchException("WndBag keeps nRows in v$rows, which is out of range")
			val scratch = if (rows == 0) 1 else 0
			addInstructions(
				index + 1,
				"""
					iget v$scratch, p0, $WND_BAG_CLASS->nCols:I
					invoke-static { p0, p1, v$scratch, v$rows }, $INVENTORY_SPACE_CLASS->rows(Ljava/lang/Object;${BAG_CLASS}II)I
					move-result v$rows
					iput v$rows, p0, $WND_BAG_CLASS->nRows:I
				""",
			)
		}
	}
}
