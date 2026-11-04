package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch

private const val MENU_CLASS = "$EXTENSION_PACKAGE/CloverlordMenu;"

internal val cloverlordMenuPatch = bytecodePatch(
	description = "Adds the Extras menu to the title screen and the in-game menu.",
) {
	extendWith("extensions/extension.mpe")

	execute {
		TitleSceneCreateFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $MENU_CLASS->onTitleCreated(Lcom/watabou/noosa/Group;)V",
		)
		WndGameConstructorFingerprint.callBeforeReturn(
			"invoke-static/range { p0 .. p0 }, $MENU_CLASS->onGameMenuCreated(Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;)V",
		)
	}
}
