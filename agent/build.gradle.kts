
plugins {
    java
    application
}

dependencies {
    testCompile("org.junit.jupiter", "junit-jupiter-api", project.extra["junitVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", project.extra["junitVersion"] as String)
    compile("com.google.code.gson", "gson", project.extra["gsonVersion"] as String)
    compile("com.tilab.jade", "jade", project.extra["jadeVersion"] as String)
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