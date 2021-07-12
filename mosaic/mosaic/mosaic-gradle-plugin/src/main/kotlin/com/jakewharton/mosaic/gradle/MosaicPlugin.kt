package com.jakewharton.mosaic.gradle

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class MosaicPlugin : KotlinCompilerPluginSupportPlugin {
	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

	override fun getCompilerPluginId() = "com.jakewharton.mosaic"

	override fun getPluginArtifact() = SubpluginArtifact(
		"com.jakewharton.mosaic",
		"compose-compiler",
		mosaicVersion,
	)

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		check(kotlinCompilation.platformType == jvm) {
			"Mosaic can only be used on JVM projects"
		}

		kotlinCompilation.dependencies {
			implementation("com.jakewharton.mosaic:mosaic-runtime:$mosaicVersion")
		}

		return kotlinCompilation.target.project.provider { emptyList() }
	}
}
