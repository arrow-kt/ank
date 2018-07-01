package arrow.ank

import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.scope.VariantScope
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class AnkAndroidPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        val extension = AnkExtension()
        extensions.add("ank", extension)
        afterEvaluate {
            plugins.findPlugin(LibraryPlugin::class.java)?.variantScopes
                ?.forEach { scope ->
                    createAnkTask(scope, extension)//.dependsOn(listOf(createUnzipTask(scope)))
                }
                ?: error("Android library plugin not found")
        }
    }

    private fun Project.createAnkTask(scope: VariantScope, extension: AnkExtension): Task? =
        task<JavaExec>(scope.variantData.getTaskName("runAnk", "")) {
            group = "ank"
            classpath = files(scope.classpath)
            main = "arrow.ank.main"
            args = ankArguments(
                source = extension.source ?: File("."), //scope.javaOutputDir,
                target = File(extension.target ?: File("."), "/${scope.fullVariantName}"),
                classpath = scope.classpath.map {
                    File(it.absolutePath.replace(".aar", ".jar"))
                }.toSet()
            )
        }.doFirst {
            scope.classpath
                .distinctBy { it -> it.name }
                .filter { it.toString().endsWith(".aar") }
                .forEach(File::unzipClassesFromArtifact)
        }

}

private fun File.unzipClassesFromArtifact() = with(ZipFile(this)) {
    entries()
        .asSequence()
        .filter { it.name == "classes.jar" }
        .forEach { entry: ZipEntry ->
            extract(from = entry, to = File(absolutePath.replace("aar", "jar")))
        }
}

private fun ZipFile.extract(from: ZipEntry, to: File) = to.apply {
    parentFile.mkdirs()
    createNewFile()
    outputStream().use { output ->
        getInputStream(from).use { input ->
            input.copyTo(output)
        }
    }
}

private val LibraryPlugin.variantScopes
    get() = variantManager.variantScopes

private inline fun <reified T : Task> Project.task(name: String, crossinline block: T.() -> Unit) =
    tasks.create(name, T::class.java) { it.block() }

private fun ankArguments(source: File, target: File, classpath: Set<File>): List<String> =
    listOf(source.absolutePath, target.absolutePath, *classpath.toPathArray())

private fun Set<File>.toPathArray(): Array<String> =
    map { it.toURI().toURL().toString() }.toTypedArray()

private val VariantScope.classpath
    get() = variantData.variantDependency.compileClasspath.resolve()

