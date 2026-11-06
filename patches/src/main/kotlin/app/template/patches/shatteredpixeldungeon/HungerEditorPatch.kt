package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val HUNGER_CLASS = "$EXTENSION_PACKAGE/HungerEditor;"

@Suppress("unused")
val hungerEditorPatch = bytecodePatch(
	name = "Hunger editor",
	description = "Adds a Hunger window for changing how long each hunger stage lasts, with presets, how often and how hard starvation hurts, or switching hunger off. Main menu settings are the default for every game; changes made during a game are saved to that game. The hunger summary on the hero info window shows the current settings and opens the window when tapped. Can be switched off in Extras, Editors.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		HungerEditorAvailableFingerprint.returnTrue()
		SaltCubeHungerGainFingerprint.adjustReturns("$HUNGER_CLASS->adjustHungerGain(F)F")

		HungerConstructorFingerprint.method.addInstruction(0, "invoke-static { }, $HUNGER_CLASS->onHungerCreated()V")
		HungerStoreInBundleFingerprint.callBeforeReturn("invoke-static/range { p1 .. p1 }, $HUNGER_CLASS->store(Lcom/watabou/utils/Bundle;)V")
		HungerRestoreFromBundleFingerprint.method.addInstruction(0, "invoke-static { p1 }, $HUNGER_CLASS->beforeRestore(Lcom/watabou/utils/Bundle;)V")
		HungerRestoreFromBundleFingerprint.callBeforeReturn("invoke-static { }, $HUNGER_CLASS->afterRestore()V")

		HungerActFingerprint.method.addInstruction(0, "invoke-static/range { p0 .. p0 }, $HUNGER_CLASS->onHungerAct(Ljava/lang/Object;)V")

		HeroDamageFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { p2 }, $HUNGER_CLASS->blocksDamage(Ljava/lang/Object;)Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)
	}
}
