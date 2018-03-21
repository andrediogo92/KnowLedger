import org.gradle.script.lang.kotlin.compile
import org.gradle.script.lang.kotlin.dependencies
import org.gradle.script.lang.kotlin.implementation
import org.gradle.script.lang.kotlin.testCompile
import java.net.URI


allprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        // maven{url = URI("http://repo.spring.io/libs-snapshot") }
        // maven{url = URI("http://repo.spring.io/milestone")}
        maven { url = URI("http://jade.tilab.com/maven") }
    }

}

plugins {
    base
}
