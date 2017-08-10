allprojects {

    group = "com.kategory.ank"

    version = "1.0"

    repositories {
        jcenter()
    }
}

plugins {
    base
}

dependencies {
    // Make the root project archives configuration depend on every sub-project
    subprojects.forEach {
        archives(it)
    }
}
