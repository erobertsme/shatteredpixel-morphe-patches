package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val SUPPORTER_CLASS = "$EXTENSION_PACKAGE/SupporterRewards;"

@Suppress("unused")
val supporterRewardsPatch = bytecodePatch(
	name = "Supporter rewards",
	description = "Adds a setting to the Extras menu that enables the Silver, Golden or Shattered supporter rewards. Only works on the Google Play version of the game.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		SupporterRewardsAvailableFingerprint.returnTrue()
		PaymentIsTierUnlockedFingerprint.returnTrueWhen("$SUPPORTER_CLASS->isTierUnlocked(I)Z", "p0")
	}
}
