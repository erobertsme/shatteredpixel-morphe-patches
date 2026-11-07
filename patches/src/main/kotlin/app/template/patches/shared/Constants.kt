package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
	val COMPATIBILITY_SHATTERED_PIXEL_DUNGEON = Compatibility(
		name = "Shattered Pixel Dungeon",
		packageName = "com.shatteredpixel.shatteredpixeldungeon",
		apkFileType = ApkFileType.APK,
		appIconColor = 0x2B2B2B,
		targets = listOf(
			AppTarget(version = "4.0.2"),
			AppTarget(version = null, isExperimental = true),
		),
	)
}
