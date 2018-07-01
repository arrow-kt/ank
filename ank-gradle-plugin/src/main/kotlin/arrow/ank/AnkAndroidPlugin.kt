package arrow.ank

import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.scope.VariantScope
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption.APPEND
import java.nio.file.StandardOpenOption.CREATE
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
                .distinctBy { it.name }
                .filter { it.toString().endsWith(".aar") }
                .map { ZipFile(it) to File(it.absolutePath.replace("aar", "jar")) }
                .mapNotNull { (zip, outFile) -> zip.unzipClassesJar(to = outFile) }
                .map(File::toFileSystem)
                .forEach(FileSystem::createManifestFile)
        }

}

private fun FileSystem.createManifestFile() = use { fs ->
    fs.getPath("META-INF/MANIFEST.MF")
        .also { path ->
            generateSequence(path.parent) { it.parent }
                .filter { Files.notExists(it) }
                .forEach { Files.createDirectory(it) }
            if (Files.notExists(path)) Files.createFile(path)
        }
        .let { Files.newBufferedWriter(it, UTF_8, CREATE, APPEND) }
        .use {
            it.write("Manifest-Version: 1.0\n")
        }
}

private fun File.toFileSystem(): FileSystem =
    URI.create("jar:${Paths.get(toURI()).toUri()}")
        .let { uri ->
            //FileSystems.getFileSystem(uri)
            try {
                FileSystems.newFileSystem(uri, mapOf("create" to "true"))
            } catch (e: java.nio.file.FileAlreadyExistsException) {
                e.printStackTrace()
                FileSystems.getFileSystem(uri)
            }
        }

private fun ZipFile.unzipClassesJar(to: File): File? = entries()
    .asSequence()
    .firstOrNull { it.name == "classes.jar" }
    ?.let { extract(from = it, to = to) }

private fun ZipFile.extract(from: ZipEntry, to: File): File = to.apply {
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

