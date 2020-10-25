plugins {
    kotlin("jvm")
    id(Plugins.base)
    id(Plugins.jmh)
}

pluginConfiguration {
    module = "collections-extensions"
    experimentalContracts = true
}

jmh {
    jmhVersion = "1.25.1"
    resultFormat = "CSV"
    jvmArgs.plusAssign("-Xms1024m")
    jvmArgs.plusAssign("-Xmx2048m")
    duplicateClassesStrategy = DuplicatesStrategy.WARN
    isIncludeTests = true
}

dependencies {
    jmhImplementation(Libs.jmh)
    testImplementation(project(":testing"))
}


version = "0.3"