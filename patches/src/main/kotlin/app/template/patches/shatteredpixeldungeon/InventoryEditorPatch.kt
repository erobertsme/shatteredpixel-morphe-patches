package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val BACKPACK_CLASS = "$EXTENSION_PACKAGE/BackpackEditor;"

@Suppress("unused")
val inventoryEditorPatch = bytecodePatch(
	name = "Inventory editor",
	description = "Adds an Inventory button to the in-game menu for editing gold and energy, adding any item in the game, and opening the backpack with an Edit button on every item (level, quantity, curses, enchantments, glyphs, augments and charges).",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		InventoryEditorAvailableFingerprint.returnTrue()

		ItemActionsFingerprint.adjustObjectReturns("$BACKPACK_CLASS->addEditAction(Ljava/util/ArrayList;)Ljava/util/ArrayList;")

		ItemActionNameFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p1 }, $BACKPACK_CLASS->actionName(Ljava/lang/String;)Ljava/lang/String;
				move-result-object p2
				if-eqz p2, :original
				return-object p2
			""",
			ExternalLabel("original", 0),
		)

		ItemExecuteFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p2 }, $BACKPACK_CLASS->execute(Ljava/lang/Object;Ljava/lang/String;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

		WndBagConstructorFingerprint.method.addInstruction(0, "invoke-static { }, $BACKPACK_CLASS->beforeBagCreated()V")
		WndBagConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $BACKPACK_CLASS->afterBagCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
		)
	}
}
