package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
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

object PatchesVersionFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/CloverlordMenu;",
	name = "patchesVersion",
	returnType = "Ljava/lang/String;",
	parameters = listOf(),
)

object PaymentIsTierUnlockedFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/services/payment/Payment;",
	name = "isTierUnlocked",
	returnType = "Z",
	parameters = listOf("I"),
)

object SupporterRewardsAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/SupporterRewards;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object TitleSceneUpdateFadeFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/TitleScene;",
	name = "updateFade",
	returnType = "V",
	parameters = listOf(),
)

object CharRemoveBuffFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/Char;",
	name = "remove",
	returnType = "Z",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/actors/buffs/Buff;"),
)

object HeroActFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "act",
	returnType = "Z",
	parameters = listOf(),
)

object WndHeroConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndHero;",
	name = "<init>",
	returnType = "V",
	parameters = listOf(),
)

object HungerEditorAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/HungerEditor;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object SaltCubeHungerGainFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/items/trinkets/SaltCube;",
	name = "hungerGainMultiplier",
	returnType = "F",
	parameters = listOf(),
)

object HeroDamageFingerprint : Fingerprint(
	definingClass = HERO_CLASS,
	name = "damage",
	returnType = "V",
	parameters = listOf("I", "Ljava/lang/Object;"),
)

object WndBagTabClickFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag;",
	name = "onClick",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndTabbed\$Tab;"),
)

object HungerActFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/buffs/Hunger;",
	name = "act",
	returnType = "Z",
	parameters = listOf(),
)

private const val CHAR_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/Char;"

object EnemyEditorAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/EnemyWindow;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object WndInfoMobConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndInfoMob;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob;"),
)

object CharStoreInBundleFingerprint : Fingerprint(
	definingClass = CHAR_CLASS,
	name = "storeInBundle",
	returnType = "V",
	parameters = listOf(BUNDLE_TYPE),
)

object CharRestoreFromBundleFingerprint : Fingerprint(
	definingClass = CHAR_CLASS,
	name = "restoreFromBundle",
	returnType = "V",
	parameters = listOf(BUNDLE_TYPE),
)

private const val HUNGER_CLASS_TYPE = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/buffs/Hunger;"

object HungerConstructorFingerprint : Fingerprint(
	definingClass = HUNGER_CLASS_TYPE,
	name = "<init>",
	returnType = "V",
	parameters = listOf(),
)

object HungerStoreInBundleFingerprint : Fingerprint(
	definingClass = HUNGER_CLASS_TYPE,
	name = "storeInBundle",
	returnType = "V",
	parameters = listOf(BUNDLE_TYPE),
)

object HungerRestoreFromBundleFingerprint : Fingerprint(
	definingClass = HUNGER_CLASS_TYPE,
	name = "restoreFromBundle",
	returnType = "V",
	parameters = listOf(BUNDLE_TYPE),
)

object FloorEditorAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/FloorWindow;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object WndInfoCellConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndInfoCell;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("I"),
)

object WndInfoTrapConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndInfoTrap;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/levels/traps/Trap;"),
)

object PathPreviewAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/PathPreview;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object DefaultCellListenerFingerprint : Fingerprint(
	name = "onSelect",
	returnType = "V",
	parameters = listOf("Ljava/lang/Integer;"),
	filters = listOf(
		methodCall(definingClass = HERO_CLASS, name = "handle"),
	),
)

internal const val SHOPKEEPER_TYPE = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/npcs/Shopkeeper;"

object ShopkeeperDamageFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "damage",
	returnType = "V",
	parameters = listOf("I", "Ljava/lang/Object;"),
)

object ShopkeeperAddFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "add",
	returnType = "Z",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/actors/buffs/Buff;"),
)

object ShopkeeperActFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "act",
	returnType = "Z",
	parameters = listOf(),
)

object ShopkeeperStoreFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "storeInBundle",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object ShopkeeperRestoreFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "restoreFromBundle",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object ShopkeeperInteractFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "interact",
	returnType = "Z",
	parameters = listOf(CHAR_CLASS),
)

object ShopkeeperFleeFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "flee",
	returnType = "V",
	parameters = listOf(),
)

object MobYellFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob;",
	name = "yell",
	returnType = "V",
	parameters = listOf("Ljava/lang/String;"),
)

object WndTradeHeapConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndTradeItem;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/Heap;"),
)

object MobGetCloserFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob;",
	name = "getCloser",
	returnType = "Z",
	parameters = listOf("I"),
)

object GameSceneCreateFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/GameScene;",
	name = "create",
	returnType = "V",
	parameters = listOf(),
)

object GameSceneBossSlainFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/GameScene;",
	name = "bossSlain",
	returnType = "V",
	parameters = listOf(),
)

object ExamineCellFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/GameScene;",
	name = "examineCell",
	returnType = "V",
	parameters = listOf("Ljava/lang/Integer;"),
)

internal const val BAG_CLASS = "Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/Bag;"

internal class BagCapacityFingerprint(definingClass: String) : Fingerprint(
	definingClass = definingClass,
	name = "capacity",
	returnType = "I",
	parameters = listOf(),
)

internal val BAG_CAPACITY_CLASSES = listOf(
	BAG_CLASS,
	"Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/MagicalHolster;",
	"Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/PotionBandolier;",
	"Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/ScrollHolder;",
	"Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/VelvetPouch;",
)

object BackpackCapacityFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/hero/Belongings\$Backpack;",
	name = "capacity",
	returnType = "I",
	parameters = listOf(),
)

object WndBagPlaceItemFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag;",
	name = "placeItem",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;"),
)

object WndBagIconFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndBag;",
	name = "icon",
	returnType = "Lcom/watabou/noosa/Image;",
	parameters = listOf(BAG_CLASS),
)

object ItemStoreInBundleFingerprint : Fingerprint(
	definingClass = ITEM_CLASS,
	name = "storeInBundle",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object ItemRestoreFromBundleFingerprint : Fingerprint(
	definingClass = ITEM_CLASS,
	name = "restoreFromBundle",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object WndInfoHeapConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndInfoItem;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/Heap;"),
)

object BagCanHoldFingerprint : Fingerprint(
	definingClass = BAG_CLASS,
	name = "canHold",
	returnType = "Z",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;"),
)

object BagCollectFingerprint : Fingerprint(
	definingClass = BAG_CLASS,
	name = "collect",
	returnType = "Z",
	parameters = listOf(BAG_CLASS),
)

object TeleportAvailableFingerprint : Fingerprint(
	definingClass = "Lapp/template/extension/shatteredpixeldungeon/Teleport;",
	name = "isAvailable",
	returnType = "Z",
	parameters = listOf(),
)

object NotesTabUpdateListFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndJournal\$NotesTab;",
	name = "updateList",
	returnType = "V",
	parameters = listOf(),
)

object NotesTabLayoutFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndJournal\$NotesTab;",
	name = "layout",
	returnType = "V",
	parameters = listOf(),
)

object ScrollingGridPaneClickFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/ui/ScrollingGridPane;",
	name = "onClick",
	returnType = "V",
	parameters = listOf("F", "F"),
)

object ResumeIndicatorUpdateFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/ui/ResumeIndicator;",
	name = "update",
	returnType = "V",
	parameters = listOf(),
)

object GameSceneLayoutTagsFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/GameScene;",
	name = "layoutTags",
	returnType = "V",
	parameters = listOf(),
)

object WndUseItemConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndUseItem;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/ui/Window;", "Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;"),
)

object GameScenePromptFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/GameScene;",
	name = "prompt",
	returnType = "V",
	parameters = listOf("Ljava/lang/String;"),
)

object WndQuickBagConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndQuickBag;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/bags/Bag;"),
)

object WndJournalItemConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndJournalItem;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/watabou/noosa/Image;", "Ljava/lang/String;", "Ljava/lang/String;"),
)

object WndInfoBuffConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndInfoBuff;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/actors/buffs/Buff;"),
)

object WndHeroStatsTabInitializeFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndHero\$StatsTab;",
	name = "initialize",
	returnType = "V",
	parameters = listOf(),
)

object MobDestroyFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob;",
	name = "destroy",
	returnType = "V",
	parameters = listOf(),
)

object MobRollToDropLootFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob;",
	name = "rollToDropLoot",
	returnType = "V",
	parameters = listOf(),
)

object MobLootChanceFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/mobs/Mob;",
	name = "lootChance",
	returnType = "F",
	parameters = listOf(),
)

object HeroEarnExpFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero;",
	name = "earnExp",
	returnType = "V",
	parameters = listOf("I", "Ljava/lang/Class;"),
)

object HeapDropFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/items/Heap;",
	name = "drop",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;"),
)

object ItemEnergyValFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;",
	name = "energyVal",
	returnType = "I",
	parameters = listOf(),
)

object RenameWindowConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/windows/WndHero\$StatsTab\$2\$1;",
	name = "<init>",
	returnType = "V",
)

object HeroReadyFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero;",
	name = "ready",
	returnType = "V",
	parameters = listOf(),
)

object DungeonDeleteGameFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/Dungeon;",
	name = "deleteGame",
	returnType = "V",
	parameters = listOf("I", "Z"),
)

object GameSceneGameOverFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/GameScene;",
	name = "gameOver",
	returnType = "V",
	parameters = listOf(),
)

object ShopRoomGenerateItemsFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/levels/rooms/special/ShopRoom;",
	name = "generateItems",
	returnType = "Ljava/util/ArrayList;",
	parameters = listOf(),
)

object ShopkeeperSellPriceFingerprint : Fingerprint(
	definingClass = SHOPKEEPER_TYPE,
	name = "sellPrice",
	returnType = "I",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/Item;"),
)

object LevelStoreFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/levels/Level;",
	name = "storeInBundle",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object LevelRestoreFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/levels/Level;",
	name = "restoreFromBundle",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object ToolbarConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/ui/Toolbar;",
	name = "<init>",
	returnType = "V",
	parameters = listOf(),
)

object ToolbarLayoutFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/ui/Toolbar;",
	name = "layout",
	returnType = "V",
	parameters = listOf(),
)

object QuickSlotStorePlaceholdersFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/QuickSlot;",
	name = "storePlaceholders",
	returnType = "V",
	parameters = listOf("Lcom/watabou/utils/Bundle;"),
)

object TrinketWindowConstructorFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/items/trinkets/TrinketCatalyst\$WndTrinket;",
	name = "<init>",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/items/trinkets/TrinketCatalyst;"),
)

object HeroGetCloserFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/actors/hero/Hero;",
	name = "getCloser",
	returnType = "Z",
	parameters = listOf("I"),
)

object DungeonSwitchLevelFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/Dungeon;",
	name = "switchLevel",
	returnType = "V",
	parameters = listOf("Lcom/shatteredpixel/shatteredpixeldungeon/levels/Level;", "I"),
)

object AndroidSafeInsetsFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/android/AndroidPlatformSupport;",
	name = "getSafeInsets",
	returnType = "Lcom/watabou/utils/RectF;",
	parameters = listOf("I"),
)

object PixelSceneDrawFingerprint : Fingerprint(
	definingClass = "Lcom/shatteredpixel/shatteredpixeldungeon/scenes/PixelScene;",
	name = "draw",
	returnType = "V",
	parameters = listOf(),
)
