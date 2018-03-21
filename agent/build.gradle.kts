import org.gradle.jvm.tasks.Jar

group = "pt.um.lei.masb"
version = "1.0"

val jadeVersion = "4.5.0"
val junitversion = "5.1.0"

plugins {
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
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}


configure<ApplicationPluginConvention> {
    mainClassName = "pt.um.lei.masb.agent.Container"
}

/**
 * Fat Jar for JADE doesn't work out-of-the-box

val fatJar = task("fatJar", type = Jar::class) {
    baseName = "${project.name}-fat"
    manifest {
        attributes["Main-Class"] = "pt.um.lei.masb.agent.Container"
    }
    from(
            configurations.runtime.map {
                if (it.isDirectory) it else zipTree(it)
            }
    )
    with(tasks["jar"] as CopySpec)
}
tasks {
    "build" {
        dependsOn(fatJar)
    }
}

*/