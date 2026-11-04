package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.Fingerprint
import com.android.tools.smali.dexlib2.AccessFlags

object BundleWriteFingerprint : Fingerprint(
	definingClass = "Lcom/watabou/utils/Bundle;",
	name = "write",
	accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
	returnType = "Z",
	parameters = listOf("Lcom/watabou/utils/Bundle;", "Ljava/io/OutputStream;", "Z"),
)

object SetDefaultFilePropertiesFingerprint : Fingerprint(
	definingClass = "Lcom/watabou/utils/FileUtils;",
	name = "setDefaultFileProperties",
	accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.STATIC),
	returnType = "V",
	parameters = listOf("L", "Ljava/lang/String;"),
)

object LauncherOnCreateFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/android/AndroidLauncher;",
	name = "onCreate",
	returnType = "V",
	parameters = listOf("Landroid/os/Bundle;"),
)

object SaveFolderNameFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/SaveLocation;",
	name = "getFolderName",
	returnType = "Ljava/lang/String;",
	parameters = listOf(),
)

object HeroClassIsUnlockedFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/hero/HeroClass;",
	name = "isUnlocked",
	returnType = "Z",
	parameters = listOf(),
)

object CatalogIsSeenFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/journal/Catalog;",
	name = "isSeen",
	returnType = "Z",
	parameters = listOf("Ljava/lang/Class;"),
)

object BestiaryIsSeenFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/journal/Bestiary;",
	name = "isSeen",
	returnType = "Z",
	parameters = listOf("Ljava/lang/Class;"),
)

private const val DOCUMENT_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/journal/Document;"

object DocumentIsPageFoundFingerprint : Fingerprint(
	definingClass = DOCUMENT_CLASS,
	name = "isPageFound",
	returnType = "Z",
	parameters = listOf("Ljava/lang/String;"),
)

object DocumentIsPageReadFingerprint : Fingerprint(
	definingClass = DOCUMENT_CLASS,
	name = "isPageRead",
	returnType = "Z",
	parameters = listOf("Ljava/lang/String;"),
)

object DocumentAnyPagesFoundFingerprint : Fingerprint(
	definingClass = DOCUMENT_CLASS,
	name = "anyPagesFound",
	returnType = "Z",
	parameters = listOf(),
)

object DocumentAllPagesFoundFingerprint : Fingerprint(
	definingClass = DOCUMENT_CLASS,
	name = "allPagesFound",
	returnType = "Z",
	parameters = listOf(),
)
