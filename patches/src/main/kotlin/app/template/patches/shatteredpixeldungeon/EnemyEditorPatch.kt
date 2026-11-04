package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val ENEMY_CLASS = "$EXTENSION_PACKAGE/EnemyEditor;"

@Suppress("unused")
val enemyEditorPatch = bytecodePatch(
	name = "Enemy editor",
	description = "Adds an Edit button when you examine an enemy, NPC or other creature, for editing its health, side, behaviour and status effects.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		EnemyEditorAvailableFingerprint.returnTrue()

		WndInfoMobConstructorFingerprint.method.addInstruction(0, "invoke-static { p1 }, $ENEMY_CLASS->beforeMobInfo(Ljava/lang/Object;)V")
		WndInfoMobConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $ENEMY_CLASS->afterMobInfo(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
		)
		CharStoreInBundleFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p1 }, $ENEMY_CLASS->store(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)
		CharRestoreFromBundleFingerprint.method.addInstruction(0, "invoke-static { p0, p1 }, $ENEMY_CLASS->beforeRestore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V")
		CharRestoreFromBundleFingerprint.callBeforeReturn("invoke-static { }, $ENEMY_CLASS->afterRestore()V")
	}
}
