package app.template.patches.shatteredpixeldungeon

import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.template.patches.shared.Constants.COMPATIBILITY_SHATTERED_PIXEL_DUNGEON
import org.w3c.dom.Element

private const val ACTIVITY_CLASS = "app.template.extension.shatteredpixeldungeon.SaveBackupActivity"
private const val ACTION_EXPORT = "app.template.extension.shatteredpixeldungeon.EXPORT_SAVES"
private const val ACTION_IMPORT = "app.template.extension.shatteredpixeldungeon.IMPORT_SAVES"
private const val SHORTCUTS_RESOURCE = "morphe_save_backup_shortcuts"
private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

private val saveBackupResourcePatch = resourcePatch {
	execute {
		val packageName = document("AndroidManifest.xml").use { document ->
			val manifest = document.documentElement
			val packageName = manifest.getAttribute("package")
			val application = document.getElementsByTagName("application").item(0) as Element

			val activity = document.createElement("activity").apply {
				setAttributeNS(ANDROID_NS, "android:name", ACTIVITY_CLASS)
				setAttributeNS(ANDROID_NS, "android:exported", "true")
				setAttributeNS(ANDROID_NS, "android:excludeFromRecents", "true")
				setAttributeNS(ANDROID_NS, "android:taskAffinity", "$packageName.savebackup")
				setAttributeNS(ANDROID_NS, "android:theme", "@android:style/Theme.Translucent.NoTitleBar")
			}
			application.appendChild(activity)

			val activities = document.getElementsByTagName("activity")
			val launcher = (0 until activities.length)
				.map { activities.item(it) as Element }
				.first { it.getAttributeNS(ANDROID_NS, "name").endsWith("AndroidLauncher") || it.getAttribute("android:name").endsWith("AndroidLauncher") }

			val metaData = document.createElement("meta-data").apply {
				setAttributeNS(ANDROID_NS, "android:name", "android.app.shortcuts")
				setAttributeNS(ANDROID_NS, "android:resource", "@xml/$SHORTCUTS_RESOURCE")
			}
			launcher.appendChild(metaData)

			packageName
		}

		get("res/values/strings.xml", false).apply {
			val text = if (exists()) readText() else "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n</resources>"
			val closing = text.lastIndexOf("</resources>")
			if (closing < 0) throw PatchException("res/values/strings.xml has no closing resources tag")

			val strings = mapOf(
				"morphe_export_saves_short" to "Export saves",
				"morphe_export_saves_long" to "Export saves to a backup file",
				"morphe_import_saves_short" to "Import saves",
				"morphe_import_saves_long" to "Import saves from a backup file",
			).entries.joinToString("") { (name, value) -> "\t<string name=\"$name\">$value</string>\n" }

			parentFile.mkdirs()
			writeText(text.substring(0, closing) + strings + text.substring(closing))
		}

		get("res/xml/$SHORTCUTS_RESOURCE.xml", false).apply {
			parentFile.mkdirs()
			writeText(
				"""
				<?xml version="1.0" encoding="utf-8"?>
				<shortcuts xmlns:android="$ANDROID_NS">
					<shortcut
						android:shortcutId="export_saves"
						android:enabled="true"
						android:icon="@mipmap/ic_launcher"
						android:shortcutShortLabel="@string/morphe_export_saves_short"
						android:shortcutLongLabel="@string/morphe_export_saves_long">
						<intent
							android:action="$ACTION_EXPORT"
							android:targetPackage="$packageName"
							android:targetClass="$ACTIVITY_CLASS" />
					</shortcut>
					<shortcut
						android:shortcutId="import_saves"
						android:enabled="true"
						android:icon="@mipmap/ic_launcher"
						android:shortcutShortLabel="@string/morphe_import_saves_short"
						android:shortcutLongLabel="@string/morphe_import_saves_long">
						<intent
							android:action="$ACTION_IMPORT"
							android:targetPackage="$packageName"
							android:targetClass="$ACTIVITY_CLASS" />
					</shortcut>
				</shortcuts>
				""".trimIndent(),
			)
		}
	}
}

@Suppress("unused")
val saveBackupPatch = bytecodePatch(
	name = "Save backup",
	description = "Adds Export saves and Import saves shortcuts to the app icon (long-press it) for backing up and restoring all saves as a zip file.",
	default = true,
) {
	compatibleWith(COMPATIBILITY_SHATTERED_PIXEL_DUNGEON)

	dependsOn(saveBackupResourcePatch)

	extendWith("extensions/extension.mpe")
}
