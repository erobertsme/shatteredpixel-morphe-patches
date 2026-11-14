package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val EXTRAS_CLASS = "$EXTENSION_PACKAGE/HeroExtras;"
private const val INFINITE_CLASS = "$EXTENSION_PACKAGE/InfiniteStatuses;"

@Suppress("unused")
val characterEditorPatch = bytecodePatch(
	name = "Character editor",
	description = "Edit your hero's stats, talents, subclass, armor ability, modifiers and status effects.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		CharacterEditorAvailableFingerprint.returnTrue()
		WndInfoBuffConstructorFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $EXTENSION_PACKAGE/BuffInfoActions;->beforeBuffInfo(Ljava/lang/Object;)V")
		WndInfoBuffConstructorFingerprint.callWithThisBeforeReturns("$EXTENSION_PACKAGE/BuffInfoActions;->afterBuffInfo(Ljava/lang/Object;)V")
		WndTabbedSelectFingerprint.callWithThisBeforeReturns("$EXTENSION_PACKAGE/CloverlordMenu;->afterTabSelected(Ljava/lang/Object;)V")
		WndHeroConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $EXTENSION_PACKAGE/CloverlordMenu;->onHeroWindowCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
		)

		HeroConstructorFingerprint.method.addInstruction(0, "invoke-static { }, $EXTRAS_CLASS->reset()V")
		HeroStoreInBundleFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $EXTRAS_CLASS->store(Lcom/watabou/utils/Bundle;)V")
		HeroRestoreFromBundleFingerprint.method.addInstruction(0, "invoke-static/range { p1 .. p1 }, $EXTRAS_CLASS->restore(Lcom/watabou/utils/Bundle;)V")

		HeroBonusTalentPointsFingerprint.method.addInstruction(0, "invoke-static { p0, p1 }, $EXTRAS_CLASS->beforeBonusTalentPoints(Ljava/lang/Object;I)V")
		HeroBonusTalentPointsFingerprint.adjustReturns("$EXTRAS_CLASS->adjustBonusTalentPoints(I)I")
		HeroAttackSkillFingerprint.adjustReturns("$EXTRAS_CLASS->adjustAccuracy(I)I")
		HeroDefenseSkillFingerprint.adjustReturns("$EXTRAS_CLASS->adjustEvasion(I)I")
		HeroDamageRollFingerprint.adjustReturns("$EXTRAS_CLASS->adjustDamage(I)I")
		HeroDrRollFingerprint.adjustReturns("$EXTRAS_CLASS->adjustArmor(I)I")
		HeroAttackDelayFingerprint.adjustReturns("$EXTRAS_CLASS->adjustAttackDelay(F)F")
		HeroSpeedFingerprint.adjustReturns("$EXTRAS_CLASS->adjustMoveSpeed(F)F")
		SaltCubeRegenFingerprint.adjustReturns("$EXTRAS_CLASS->adjustRegen(F)F")

		CharRemoveBuffFingerprint.method.addInstruction(0, "invoke-static { p0, p1 }, $INFINITE_CLASS->beforeRemove(Ljava/lang/Object;Ljava/lang/Object;)V")
		CharRemoveBuffFingerprint.adjustReturns("$INFINITE_CLASS->afterRemove(Z)Z")
		HeroActFingerprint.method.addInstruction(0, "invoke-static/range { p0 .. p0 }, $INFINITE_CLASS->onHeroAct(Ljava/lang/Object;)V")
	}
}
