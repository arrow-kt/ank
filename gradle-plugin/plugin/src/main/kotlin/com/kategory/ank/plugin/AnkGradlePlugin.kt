package com.kategory.ank.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec

import org.gradle.kotlin.dsl.*

open class AnkGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.run {

            tasks {
                "myEchoTask"(Exec::class) {
                    executable("sh")
                    args("-c", "echo 'Hello ΛNK from your shell!'")
                }
            }
        }
    }
}
