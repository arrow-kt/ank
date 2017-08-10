plugins {
    kotlin("jvm")
    application
}

kotlinProject()

application {
    mainClassName = "com.kategory.ank.cli.Main"
}

dependencies {
    compile(project(":core"))
}
