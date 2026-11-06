package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val PREVIEW_CLASS = "$EXTENSION_PACKAGE/PathPreview;"
private const val SHOPKEEPER_CLASS = "$EXTENSION_PACKAGE/ShopkeeperRules;"
private const val CHEST_CLASS = "$EXTENSION_PACKAGE/QuantumChest;"
private const val QUICKSLOT_CLASS = "$EXTENSION_PACKAGE/QuickslotRow;"
private const val ITEM_TYPE = "Ljava/lang/Object;"

@Suppress("unused")
val qolEnhancementsPatch = bytecodePatch(
	name = "QoL enhancements",
	description = "Confirm risky taps, a calmer shopkeeper, a Quantum Chest on boss floors, bigger bags and an extra quickslot row.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch, bagLayoutPatch)

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

		ShopkeeperDamageFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p2 }, $SHOPKEEPER_CLASS->onDamage(Ljava/lang/Object;Ljava/lang/Object;)Z
				move-result p1
				if-eqz p1, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

		ShopkeeperAddFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p1 }, $SHOPKEEPER_CLASS->onBuff(Ljava/lang/Object;Ljava/lang/Object;)Z
				move-result v0
				if-eqz v0, :original
				const/4 v0, 0x0
				return v0
			""",
			ExternalLabel("original", 0),
		)

		ShopkeeperActFingerprint.method.addInstruction(0, "invoke-static { p0 }, $SHOPKEEPER_CLASS->onAct(Ljava/lang/Object;)V")
		ShopkeeperStoreFingerprint.method.addInstruction(0, "invoke-static { p0, p1 }, $SHOPKEEPER_CLASS->onStore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V")
		ShopkeeperRestoreFingerprint.method.addInstruction(0, "invoke-static { p0, p1 }, $SHOPKEEPER_CLASS->onRestore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V")

		ShopkeeperInteractFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p1 }, $SHOPKEEPER_CLASS->onInteract(Ljava/lang/Object;Ljava/lang/Object;)Z
				move-result v0
				if-eqz v0, :original
				const/4 v0, 0x1
				return v0
			""",
			ExternalLabel("original", 0),
		)

		ShopkeeperFleeFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0 }, $SHOPKEEPER_CLASS->onFlee(Ljava/lang/Object;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

		MobYellFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p1 }, $SHOPKEEPER_CLASS->onYell(Ljava/lang/Object;Ljava/lang/String;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

		WndTradeHeapConstructorFingerprint.method.addInstruction(
			0,
			"invoke-static/range { p1 .. p1 }, $SHOPKEEPER_CLASS->onTradeWindow(Ljava/lang/Object;)V",
		)

		LauncherOnCreateFingerprint.method.addInstruction(0, "invoke-static { }, $QUICKSLOT_CLASS->onLaunch()V")
		ItemStoreInBundleFingerprint.method.addInstruction(
			0,
			"invoke-static { p0, p1 }, $QUICKSLOT_CLASS->beforeItemStore(${ITEM_TYPE}Lcom/watabou/utils/Bundle;)V",
		)
		ItemStoreInBundleFingerprint.callBeforeEveryReturn("invoke-static { p0 }, $QUICKSLOT_CLASS->afterItemStore($ITEM_TYPE)V")
		ItemRestoreFromBundleFingerprint.method.addInstruction(
			0,
			"invoke-static { p0, p1 }, $QUICKSLOT_CLASS->beforeItemRestore(${ITEM_TYPE}Lcom/watabou/utils/Bundle;)V",
		)
		ItemRestoreFromBundleFingerprint.callBeforeEveryReturn("invoke-static { p0 }, $QUICKSLOT_CLASS->afterItemRestore($ITEM_TYPE)V")
		QuickSlotStorePlaceholdersFingerprint.method.addInstruction(0, "invoke-static { }, $QUICKSLOT_CLASS->beforePlaceholders()V")
		QuickSlotStorePlaceholdersFingerprint.callBeforeEveryReturn("invoke-static { }, $QUICKSLOT_CLASS->afterPlaceholders()V")
		ToolbarConstructorFingerprint.callBeforeEveryReturn("invoke-static/range { p0 .. p0 }, $QUICKSLOT_CLASS->afterToolbarCreated($ITEM_TYPE)V")
		ToolbarLayoutFingerprint.callBeforeEveryReturn("invoke-static/range { p0 .. p0 }, $QUICKSLOT_CLASS->afterToolbarLayout($ITEM_TYPE)V")

		GameSceneCreateFingerprint.callBeforeEveryReturn(
			"invoke-static/range { p0 .. p0 }, $CHEST_CLASS->onSceneCreated(Ljava/lang/Object;)V",
		)
		GameSceneBossSlainFingerprint.method.addInstruction(0, "invoke-static { }, $CHEST_CLASS->onBossSlain()V")
		HeroStoreInBundleFingerprint.method.addInstruction(
			0,
			"invoke-static/range { p0 .. p1 }, $CHEST_CLASS->onStore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)
		HeroRestoreFromBundleFingerprint.method.addInstruction(
			0,
			"invoke-static/range { p0 .. p1 }, $CHEST_CLASS->onRestore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)

		MobGetCloserFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p1 }, $SHOPKEEPER_CLASS->blockStep(Ljava/lang/Object;I)Z
				move-result v0
				if-eqz v0, :original
				const/4 v0, 0x0
				return v0
			""",
			ExternalLabel("original", 0),
		)
	}
}
