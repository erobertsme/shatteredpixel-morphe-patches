package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val PREVIEW_CLASS = "$EXTENSION_PACKAGE/PathPreview;"
private const val SHOPKEEPER_CLASS = "$EXTENSION_PACKAGE/FearlessShopkeeper;"

@Suppress("unused")
val qolEnhancementsPatch = bytecodePatch(
	name = "QoL enhancements",
	description = "Quality of life options in the Extras menu. Confirm actions: a tap that would attack, walk somewhere, use stairs or step into danger first shows what will happen, with Go and Cancel buttons. Fearless shopkeeper: the shopkeeper never runs off with the shop.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

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

		ShopkeeperFleeFingerprint.method.addInstructionsWithLabels(
			0,
			"""
				invoke-static { }, $SHOPKEEPER_CLASS->stays()Z
				move-result v0
				if-eqz v0, :original
				return-void
			""",
			ExternalLabel("original", 0),
		)
	}
}
