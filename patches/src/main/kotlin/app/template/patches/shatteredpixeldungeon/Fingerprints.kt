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

object TitleSceneCreateFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/TitleScene;",
	name = "create",
	returnType = "V",
	parameters = listOf(),
)

object WndGameConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndGame;",
	name = "<init>",
	returnType = "V",
	parameters = listOf(),
)

object UnlockAllAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/UnlockAll;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object SaveBackupAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/SaveBackupActivity;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

private const val HERO_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero;"
private const val CHAR_TYPE = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/Char;"
private const val BUNDLE_TYPE = "Lcom/watabou/utils/Bundle;"

object HeroConstructorFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "<init>",
	returnType = "V",
	parameters = listOf(),
)

object HeroStoreInBundleFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "storeInBundle",
	returnType = "V",
	parameters = listOf(BUNDLE_TYPE),
)

object HeroRestoreFromBundleFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "restoreFromBundle",
	returnType = "V",
	parameters = listOf(BUNDLE_TYPE),
)

object HeroBonusTalentPointsFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "bonusTalentPoints",
	returnType = "I",
	parameters = listOf("I"),
)

object HeroAttackSkillFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "attackSkill",
	returnType = "I",
	parameters = listOf(CHAR_TYPE),
)

object HeroDefenseSkillFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "defenseSkill",
	returnType = "I",
	parameters = listOf(CHAR_TYPE),
)

object HeroDamageRollFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "damageRoll",
	returnType = "I",
	parameters = listOf(),
)

object HeroDrRollFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "drRoll",
	returnType = "I",
	parameters = listOf(),
)

object HeroAttackDelayFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "attackDelay",
	returnType = "F",
	parameters = listOf(),
)

object HeroSpeedFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "speed",
	returnType = "F",
	parameters = listOf(),
)

object SaltCubeRegenFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/items/trinkets/SaltCube;",
	name = "healthRegenMultiplier",
	returnType = "F",
	parameters = listOf(),
)

object CharacterEditorAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/CharacterWindow;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object InventoryEditorAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/InventoryWindow;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

private const val ITEM_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;"

object ItemActionsFingerprint : Fingerprint(
	definingClass = ITEM_CLASS,
	name = "actions",
	returnType = "Ljava/util/ArrayList;",
	parameters = listOf(HERO_CLASS),
)

object ItemActionNameFingerprint : Fingerprint(
	definingClass = ITEM_CLASS,
	name = "actionName",
	returnType = "Ljava/lang/String;",
	parameters = listOf("Ljava/lang/String;", HERO_CLASS),
)

object ItemExecuteFingerprint : Fingerprint(
	definingClass = ITEM_CLASS,
	name = "execute",
	returnType = "V",
	parameters = listOf(HERO_CLASS, "Ljava/lang/String;"),
)

object WndBagConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag;",
	name = "<init>",
	returnType = "V",
	parameters = listOf(
		"Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/Bag;",
		"Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag\$ItemSelector;",
	),
)
