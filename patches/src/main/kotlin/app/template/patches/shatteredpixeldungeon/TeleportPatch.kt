package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val TELEPORT_CLASS = "$EXTENSION_PACKAGE/Teleport;"

@Suppress("unused")
val teleportPatch = bytecodePatch(
	name = "Teleport",
	description = "Adds Teleport to the in-game menu and a Teleport to... button in the Adventuring Notes for travelling to any floor, arriving at its entrance or its exit. Also adds Teleport Here for the hero in the Floor editor and on the tile examine window, and Teleport for creatures in the Enemy and NPC editor. Can be switched off in Extras, Editors.",
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
