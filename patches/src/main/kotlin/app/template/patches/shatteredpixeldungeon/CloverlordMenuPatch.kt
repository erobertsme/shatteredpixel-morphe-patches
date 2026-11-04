package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.PATCHES_VERSION

private const val MENU_CLASS = "$EXTENSION_PACKAGE/CloverlordMenu;"

internal val cloverlordMenuPatch = bytecodePatch(
	description = "Adds the Extras menu to the title screen and the in-game menu.",
) {
	extendWith("extensions/extension.mpe")

	execute {
		PatchesVersionFingerprint.method.addInstructions(
			0,
			"""
				const-string v0, "$PATCHES_VERSION"
				return-object v0
			""",
		)

		TitleSceneCreateFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $MENU_CLASS->onTitleCreated(Lcom/watabou/noosa/Group;)V",
		)
		WndGameConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $MENU_CLASS->onGameMenuCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
		)
	}
}
