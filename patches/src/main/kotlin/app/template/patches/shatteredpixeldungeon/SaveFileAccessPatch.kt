package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON

private const val PROVIDER_CLASS = "app.template.extension.shatteredpixeldungeon.SaveFilesProvider"
private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

private val saveFilesProviderManifestPatch = resourcePatch {
	execute {
		document("AndroidManifest.xml").use { document ->
			val manifest = document.documentElement
			val packageName = manifest.getAttribute("package")
			val application = document.getElementsByTagName("application").item(0)

			val provider = document.createElement("provider").apply {
				setAttributeNS(ANDROID_NS, "android:name", PROVIDER_CLASS)
				setAttributeNS(ANDROID_NS, "android:authorities", "$packageName.savefiles")
				setAttributeNS(ANDROID_NS, "android:exported", "true")
				setAttributeNS(ANDROID_NS, "android:grantUriPermissions", "true")
				setAttributeNS(ANDROID_NS, "android:permission", "android.permission.MANAGE_DOCUMENTS")
			}

			val intentFilter = document.createElement("intent-filter")
			val action = document.createElement("action").apply {
				setAttributeNS(ANDROID_NS, "android:name", "android.content.action.DOCUMENTS_PROVIDER")
			}
			intentFilter.appendChild(action)
			provider.appendChild(intentFilter)
			application.appendChild(provider)
		}
	}
}

@Suppress("unused")
val saveFileAccessPatch = bytecodePatch(
	name = "Save file access",
	description = "Exposes the game's save folder in the system file picker and Files app so saves can be copied, backed up, and edited.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(saveFilesProviderManifestPatch)

	extendWith("extensions/extension.mpe")
}
