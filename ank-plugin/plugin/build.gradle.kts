plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `maven-publish`
}

group = "ank"
version = "1.0"

gradlePlugin {
    (plugins) {
        "ankPlugin" {
            id = "ank-plugin"
            implementationClass = "plugin.AnkPlugin"
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
