package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.booleanOption
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.smali.ExternalLabel
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val EXTENSION_CLASS = "Lapp/template/extension/shatteredpixeldungeon/UnlockAll;"

private val documentFingerprints = listOf(
	DocumentIsPageFoundFingerprint,
	DocumentIsPageReadFingerprint,
	DocumentAnyPagesFoundFingerprint,
	DocumentAllPagesFoundFingerprint,
)

context(_: BytecodePatchContext)
private fun Fingerprint.returnTrue() {
	method.addInstructions(
		0,
		"""
			const/4 p0, 0x1
			return p0
		""",
	)
}

context(_: BytecodePatchContext)
private fun Fingerprint.returnTrueForDocuments(guides: Boolean) {
	val method = this.method
	val implementation = method.implementation ?: throw PatchException("${method.name} has no code")
	val parameterRegisters = method.parameters.sumOf { if (it.type == "J" || it.type == "D") 2 else 1 } + 1
	if (implementation.registerCount <= parameterRegisters) throw PatchException("${method.name} has no free register")

	val skip = if (guides) "if-eqz" else "if-nez"
	method.addInstructionsWithLabels(
		0,
		"""
			invoke-static { p0 }, $EXTENSION_CLASS->isGuideDocument(Ljava/lang/Object;)Z
			move-result v0
			$skip v0, :original
			const/4 v0, 0x1
			return v0
		""",
		ExternalLabel("original", 0),
	)
}

@Suppress("unused")
val unlockAllPatch = bytecodePatch(
	name = "Unlock all",
	description = "Unlocks all heroes, journal pages and catalog entries without changing your save data. Removing the patch restores your real progress.",
	default = false,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	extendWith("extensions/extension.mpe")

	val heroes by booleanOption(
		key = "heroes",
		default = true,
		title = "Heroes",
		description = "Unlock all hero classes.",
	)
	val guides by booleanOption(
		key = "guides",
		default = true,
		title = "Guide books",
		description = "Unlock every page of the Adventurer's Guide and the Alchemy Guide.",
	)
	val lore by booleanOption(
		key = "lore",
		default = true,
		title = "Intros and lore",
		description = "Unlock the region intros and every lore document.",
	)
	val items by booleanOption(
		key = "items",
		default = true,
		title = "Item catalog",
		description = "Mark every item in the journal catalog as seen.",
	)
	val bestiary by booleanOption(
		key = "bestiary",
		default = true,
		title = "Bestiary",
		description = "Mark every creature, trap and plant in the bestiary as seen.",
	)

	execute {
		if (heroes == true) HeroClassIsUnlockedFingerprint.returnTrue()
		if (items == true) CatalogIsSeenFingerprint.returnTrue()
		if (bestiary == true) BestiaryIsSeenFingerprint.returnTrue()

		val unlockGuides = guides == true
		val unlockLore = lore == true
		if (unlockGuides && unlockLore) {
			documentFingerprints.forEach { it.returnTrue() }
			return@execute
		}
		if (unlockGuides) documentFingerprints.forEach { it.returnTrueForDocuments(guides = true) }
		if (unlockLore) documentFingerprints.forEach { it.returnTrueForDocuments(guides = false) }
	}
}
