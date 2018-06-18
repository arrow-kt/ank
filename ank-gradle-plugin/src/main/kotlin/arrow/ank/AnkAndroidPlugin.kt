package arrow.ank

import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class AnkAndroidPlugin : Plugin<Project> {

    companion object {
        private const val EXTENSION_NAME = "ank"
        private const val TASK_NAME = "runAnk"
    }

    override fun apply(target: Project) {
        val extension = AnkExtension()
        target.extensions.add(EXTENSION_NAME, extension)
        target.afterEvaluate {
            target.plugins.findPlugin(LibraryPlugin::class.java)?.variantManager?.variantDataList?.forEach({ variant ->
                target.tasks.create("${variant.name}$TASK_NAME", JavaExec::class.java).apply {

//                    classpath = target.files(variant.variantConfiguration)
                    main = "arrow.ank.main"
                }.args = mutableListOf(
                        extension.source!!.absolutePath,
                        extension.target!!.absolutePath + "/" + variant.name,
                        ""//*variant.variantConfiguration.compileClasspath.map { it.toURI().toURL().toString() }.toTypedArray()
                )
            })
        }
    }
}
