package arrow.ank

import org.gradle.testkit.runner.GradleRunner
import org.junit.Test
import java.io.File

class AnkAndroidPluginTest {

    @Test
    fun `simple test`() {
        val rootDirectory = generateSequence(File(javaClass.classLoader.getResource(".").toURI())) { it.parentFile }
            .dropWhile { !it.name.endsWith("build") }.drop(2)
            .firstOrNull()
        val testProjectDir = rootDirectory?.let {
            it.listFiles().first { it.name.endsWith("android-sample") }
        }

        GradleRunner.create()
            .withProjectDir(rootDirectory)
            .forwardStdError(System.err.writer())
            .forwardStdOutput(System.out.writer())
            .build()

    }

}
