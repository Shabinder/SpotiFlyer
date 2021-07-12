package com.jakewharton.mosaic.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.File

@RunWith(Parameterized::class)
class FixtureTest(
	private val fixtureName: String,
) {
	companion object {
		@JvmStatic
		@Parameters(name = "{0}")
		fun parameters() = listOf(
			arrayOf("counter"),
		)
	}

	private val fixturesDir = File("src/test/fixture")
	private fun versionProperty() = "-PmosaicVersion=$mosaicVersion"

	@Test fun todo() {
		val fixtureDir = File(fixturesDir, fixtureName)
		val gradleRoot = File(fixtureDir, "gradle").also { it.mkdir() }
		File("../../gradle/wrapper").copyRecursively(File(gradleRoot, "wrapper"), true)

		GradleRunner.create()
			.withProjectDir(fixtureDir)
			.withArguments("clean", "build", "--stacktrace", versionProperty())
			.build()
	}
}
