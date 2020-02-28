plugins {
    kotlin("jvm")
    id(Plugins.base)
    id(Plugins.jmh)
}

basePlugin {
    module = "collections-extensions"
}

jmh {
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    isIncludeTests = true
}

dependencies {
    jmhImplementation(Libs.jmh)

    testImplementation(project(":testing"))
}


version = "0.1"