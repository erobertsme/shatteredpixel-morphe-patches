package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val TELEPORT_CLASS = "$EXTENSION_PACKAGE/Teleport;"

@Suppress("unused")
val teleportPatch = bytecodePatch(
	name = "Teleport",
	description = "Teleport features: a Teleport to... button in the Adventuring Notes for travelling to any floor, Teleport Here for the hero in the Floor editor, and Teleport for creatures in the Enemy and NPC editor.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		TeleportAvailableFingerprint.returnTrue()

		NotesTabUpdateListFingerprint.callBeforeEveryReturn(
			"invoke-static/range { p0 .. p0 }, $TELEPORT_CLASS->afterNotesLayout(Ljava/lang/Object;)V",
		)
		NotesTabLayoutFingerprint.callBeforeEveryReturn(
			"invoke-static/range { p0 .. p0 }, $TELEPORT_CLASS->afterNotesLayout(Ljava/lang/Object;)V",
		)
	}
}
