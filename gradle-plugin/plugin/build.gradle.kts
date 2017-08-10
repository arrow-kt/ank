plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "com.kategory.ank"
version = "1.0"

gradlePlugin {
    (plugins) {
        "ankGradlePlugin" {
            id = "ank-gradle-plugin"
            implementationClass = "com.kategory.ank.plugin.AnkGradlePlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("build/repository")
        }
    }
}
