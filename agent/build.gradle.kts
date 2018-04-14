val jadeVersion = "4.5.0"
val junitversion = "5.1.0"

plugins {
    java
    application
}

dependencies {
    testCompile("org.junit.jupiter", "junit-jupiter-api", junitversion)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", junitversion)
    compile("com.google.code.gson", "gson", "2.8.2")
    compile("com.tilab.jade", "jade", jadeVersion)
    compile("com.tilab.jade", "jade-misc", "2.8.0")
    compile("com.tilab.jade", "jade-xml-codec", "1.11.0")
    compile("com.tilab.jade", "jade-test-suite", "1.13.0")
    compile(project(":blockchain"))
}


tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}


configure<ApplicationPluginConvention> {
    mainClassName = "pt.um.lei.masb.agent.Container"
}