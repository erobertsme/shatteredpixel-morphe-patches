package app.template.patches.shatteredpixeldungeon

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

		WndInfoMobConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p1 }, $ENEMY_CLASS->onMobInfoCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;Ljava/lang/Object;)V",
		)
		CharStoreInBundleFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p1 }, $ENEMY_CLASS->store(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)
		CharRestoreFromBundleFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p1 }, $ENEMY_CLASS->restore(Ljava/lang/Object;Lcom/watabou/utils/Bundle;)V",
		)
	}
}
