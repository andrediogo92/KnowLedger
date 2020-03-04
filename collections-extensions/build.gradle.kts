plugins {
    kotlin("jvm")
    id(Plugins.base)
    id(Plugins.jmh)
}

basePlugin {
    module = "collections-extensions"
}

jmh {
    jvmArgs.plusAssign("-Xms1024m")
    jvmArgs.plusAssign("-Xmx2048m")
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    isIncludeTests = true
}

dependencies {
    jmhImplementation(Libs.jmh)
    testImplementation(project(":testing"))
}


version = "0.2"