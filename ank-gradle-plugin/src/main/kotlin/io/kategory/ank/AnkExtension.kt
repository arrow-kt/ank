package io.kategory.ank

import org.gradle.api.file.FileCollection


data class AnkExtension(var classpath: FileCollection? = null, var arguments: List<String> = emptyList())