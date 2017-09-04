package io.kategory.ank

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.tasks.JavaExec


class AnkPlugin : Plugin<Project> {

    companion object {
        private val EXTENSION_NAME = "ank"
        private val TASK_NAME = "runAnk"
        private val ANK_CORE_DEPENDENCY = "io.kategory:ank-core:0.1.3"
    }

    override fun apply(target: Project) {
        val compileDeps = target.configurations.getByName("compile").dependencies
        target.gradle.addListener(object : DependencyResolutionListener {
            override fun beforeResolve(resolvableDependencies: ResolvableDependencies) {
                compileDeps.add(target.dependencies.create(ANK_CORE_DEPENDENCY))
                target.gradle.removeListener(this)
            }

            override fun afterResolve(resolvableDependencies: ResolvableDependencies) {}
        })
        val extension = AnkExtension()
        target.extensions.add(EXTENSION_NAME, extension)
        target.afterEvaluate { _ ->
            val task = target.tasks.create(TASK_NAME, JavaExec::class.java)
            task.classpath = extension.classpath
            task.main = "io.kategory.ank.main"
            val args = mutableListOf(
                    extension.source!!.absolutePath,
                    extension.target!!.absolutePath,
                    *extension.classpath!!.map { it.toURI().toURL().toString() }.toTypedArray()
            )
            task.setArgs(args)
        }
    }
}