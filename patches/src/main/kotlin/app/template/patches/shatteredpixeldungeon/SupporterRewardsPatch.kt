package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val SUPPORTER_CLASS = "$EXTENSION_PACKAGE/SupporterRewards;"

@Suppress("unused")
val supporterRewardsPatch = bytecodePatch(
	name = "Supporter options",
	description = "NOTICE: This patch is not intended to gain these bonus extras for free. It is meant to ensure supporters are able to keep the benefits they paid for. Don't be a scumbag. Please support the developer!\n\nAdds Supporter options to the Extras menu: keep your Silver, Golden or Shattered supporter rewards, and hide the Support button on the title screen. Only works on the Google Play version of the game.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		SupporterRewardsAvailableFingerprint.returnTrue()
		PaymentIsTierUnlockedFingerprint.returnTrueWhen("$SUPPORTER_CLASS->isTierUnlocked(I)Z", "p0")
	}
}
