import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.implementation
import org.gradle.script.lang.kotlin.testCompile
import java.net.URI


subprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"


    apply{
        plugin("java")
        plugin("idea")
    }


    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
    }

    repositories {
        mavenCentral()
        jcenter()
        maven{url = URI("http://repo.spring.io/libs-snapshot") }
        maven{url = URI("http://repo.spring.io/milestone")}
        maven{url = URI("http://jade.tilab.com/maven")}
    }

    dependencies {
        testCompile("junit", "junit", "4.12")
    }
}

project(":blockchain") {
    val vertxVersion = "3.5.1"

    dependencies {
        compile("io.vertx", "vertx-mongo-client", vertxVersion)
        implementation("com.google.code.gson", "gson", "2.8.2")
        implementation("org.bouncycastle", "bcprov-jdk15on", "1.59")
    }
}

project(":agent") {
    val jadeVersion = "4.5.0"

    dependencies {
        compile("com.tilab.jade", "jade", jadeVersion)
        compile("com.tilab.jade", "jade-misc", "2.8.0")
        compile("com.tilab.jade", "jade-xml-codec","1.11.0")
        compile("com.tilab.jade", "jade-test-suite", "1.13.0")
        compile("com.tilab.jade", "jade-wsdc", jadeVersion)
        compile(project(":blockchain"))
    }
}
