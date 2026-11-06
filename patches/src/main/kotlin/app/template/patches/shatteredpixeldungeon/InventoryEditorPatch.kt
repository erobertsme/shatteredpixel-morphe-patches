package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val BACKPACK_CLASS = "$EXTENSION_PACKAGE/BackpackEditor;"
private const val INFINITE_CHARGES_CLASS = "$EXTENSION_PACKAGE/InfiniteCharges;"

@Suppress("unused")
val inventoryEditorPatch = bytecodePatch(
	name = "Inventory editor",
	description = "Edit gold, energy and every item, add any item, and identify or uncurse everything. Adds unlimited inventory space and Weapons, Armor and Rings backpack tabs.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch, bagLayoutPatch)

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
		WndBagTabClickFingerprint.method.addInstruction(0, "invoke-static { p0 }, $BACKPACK_CLASS->beforeTabSwitch(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V")
		WndBagConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $BACKPACK_CLASS->afterBagCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
		)

		ItemStoreInBundleFingerprint.method.addInstruction(
			0,
			"invoke-static { p0, p1 }, $INFINITE_CHARGES_CLASS->onStore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)
		ItemRestoreFromBundleFingerprint.method.addInstruction(
			0,
			"invoke-static { p0, p1 }, $INFINITE_CHARGES_CLASS->onRestore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)
		HeroActFingerprint.method.addInstruction(
			0,
			"invoke-static/range { p0 .. p0 }, $INFINITE_CHARGES_CLASS->onHeroAct(Ljava/lang/Object;)V",
		)
	}
}
