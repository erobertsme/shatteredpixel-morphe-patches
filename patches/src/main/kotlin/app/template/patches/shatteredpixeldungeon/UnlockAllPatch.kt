package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val UNLOCK_CLASS = "$EXTENSION_PACKAGE/UnlockAll;"

private val documentFingerprints = listOf(
	DocumentIsPageFoundFingerprint,
	DocumentIsPageReadFingerprint,
	DocumentAnyPagesFoundFingerprint,
	DocumentAllPagesFoundFingerprint,
)

@Suppress("unused")
val unlockAllPatch = bytecodePatch(
	name = "Unlock all",
	description = "Adds toggles to the Cloverlord menu that unlock all heroes, guide books, intros and lore, the item catalog and the bestiary. Your save data is not changed, so turning a toggle off restores your real progress.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(cloverlordMenuPatch)

	execute {
		UnlockAllAvailableFingerprint.returnTrue()

		HeroClassIsUnlockedFingerprint.returnTrueWhen("$UNLOCK_CLASS->unlockHeroes()Z")
		CatalogIsSeenFingerprint.returnTrueWhen("$UNLOCK_CLASS->unlockItems()Z")
		BestiaryIsSeenFingerprint.returnTrueWhen("$UNLOCK_CLASS->unlockBestiary()Z")
		documentFingerprints.forEach {
			it.returnTrueWhen("$UNLOCK_CLASS->isDocumentUnlocked(Ljava/lang/Object;)Z", "p0")
		}
	}
}
