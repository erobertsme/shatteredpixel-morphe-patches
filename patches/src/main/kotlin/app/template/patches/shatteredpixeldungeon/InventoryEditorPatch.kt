package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.TwoRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val BACKPACK_CLASS = "$EXTENSION_PACKAGE/BackpackEditor;"
private const val INVENTORY_SPACE_CLASS = "$EXTENSION_PACKAGE/InventorySpace;"
private const val INFINITE_CHARGES_CLASS = "$EXTENSION_PACKAGE/InfiniteCharges;"
private const val WND_BAG_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag;"

@Suppress("unused")
val inventoryEditorPatch = bytecodePatch(
	name = "Inventory editor",
	description = "Adds an Inventory button to the in-game menu for editing gold and energy, adding any item in the game, and opening the backpack with an Edit button on every item (level, quantity, curses, enchantments, glyphs, augments and charges). Also adds optional unlimited inventory space with pages, and Weapons, Armor and Rings backpack tabs.",
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
		WndBagTabClickFingerprint.method.addInstruction(0, "invoke-static { p0 }, $BACKPACK_CLASS->beforeTabSwitch(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V")
		WndBagConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $BACKPACK_CLASS->afterBagCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
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

		WndBagConstructorFingerprint.method.apply {
			val instructions = implementation!!.instructions.toList()
			val index = instructions.indexOfFirst {
				it.opcode == Opcode.INVOKE_VIRTUAL && ((it as ReferenceInstruction).reference as MethodReference).name == "getBags"
			}
			if (index < 0 || instructions[index + 1].opcode != Opcode.MOVE_RESULT_OBJECT) throw PatchException("WndBag does not list the bags")
			val bags = (instructions[index + 1] as OneRegisterInstruction).registerA
			addInstructions(
				index + 2,
				"""
					invoke-static/range { v$bags .. v$bags }, $INVENTORY_SPACE_CLASS->bagTabs(Ljava/util/ArrayList;)Ljava/util/ArrayList;
					move-result-object v$bags
				""",
			)
		}

		WndBagPlaceItemFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p1 }, $INVENTORY_SPACE_CLASS->skipSlot(Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)

		BagCollectFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p0, p1 }, $INVENTORY_SPACE_CLASS->refuseBag(${BAG_CLASS}$BAG_CLASS)Z
				move-result v0
				if-eqz v0, :original
				const/4 v0, 0x0
				return v0
			""",
			ExternalLabel("original", 0),
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

		WndBagIconFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p1 }, $INVENTORY_SPACE_CLASS->tabIcon($BAG_CLASS)Lcom/watabou/noosa/Image;
				move-result-object v0
				if-eqz v0, :original
				return-object v0
			""",
			ExternalLabel("original", 0),
		)
	}
}
