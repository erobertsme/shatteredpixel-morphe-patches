package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val EXTENSION_CLASS = "Lapp/template/extension/shatteredpixeldungeon/SaveLocation;"
private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

private val storagePermissionManifestPatch = resourcePatch {
	execute {
		document("AndroidManifest.xml").use { document ->
			val manifest = document.documentElement
			val application = document.getElementsByTagName("application").item(0) as org.w3c.dom.Element

			fun addPermission(name: String, maxSdk: Int? = null) {
				val permission = document.createElement("uses-permission").apply {
					setAttributeNS(ANDROID_NS, "android:name", name)
					if (maxSdk != null) setAttributeNS(ANDROID_NS, "android:maxSdkVersion", maxSdk.toString())
				}
				manifest.insertBefore(permission, application)
			}

			addPermission("android.permission.MANAGE_EXTERNAL_STORAGE")
			addPermission("android.permission.WRITE_EXTERNAL_STORAGE", 29)
			addPermission("android.permission.READ_EXTERNAL_STORAGE", 29)
			application.setAttributeNS(ANDROID_NS, "android:requestLegacyExternalStorage", "true")
		}
	}
}

@Suppress("unused")
val publicSaveFolderPatch = bytecodePatch(
	name = "Public save folder",
	description = "Stores save files in a public folder on the device so they can be opened with any file manager. Asks for storage access on first launch and copies existing saves over.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(storagePermissionManifestPatch)

	extendWith("extensions/extension.mpe")

	val folderName by stringOption(
		key = "folderName",
		default = "Documents/ShatteredPixelDungeon",
		title = "Save folder",
		description = "Folder relative to the root of the device storage.",
		required = true,
	) { !it.isNullOrBlank() && !it.contains('"') && !it.contains('\\') }

	execute {
		val folder = folderName!!.trim('/', ' ')

		SaveFolderNameFingerprint.method.addInstructions(
			0,
			"""
				const-string v0, "$folder"
				return-object v0
			""",
		)

		val setDefaults = SetDefaultFilePropertiesFingerprint.method
		val fileType = setDefaults.parameterTypes.first().toString()
		if (!fileType.startsWith("L")) throw PatchException("Unexpected file type parameter: $fileType")

		setDefaults.addInstructions(
			0,
			"""
				invoke-static { p0 }, $EXTENSION_CLASS->getFileType(Ljava/lang/Object;)Ljava/lang/Object;
				move-result-object p0
				check-cast p0, $fileType
				invoke-static { p1 }, $EXTENSION_CLASS->getBasePath(Ljava/lang/String;)Ljava/lang/String;
				move-result-object p1
			""",
		)

		LauncherOnCreateFingerprint.method.addInstruction(
			0,
			"invoke-static { p0 }, $EXTENSION_CLASS->onActivityCreated(Landroid/app/Activity;)V",
		)
	}
}
