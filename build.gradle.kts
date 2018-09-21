import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    val _kotlin_version = "1.2.70"

    repositories {
        mavenCentral()
        mavenLocal()
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", _kotlin_version))
        classpath(kotlin("noarg", _kotlin_version))
    }

}


subprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven("http://jade.tilab.com/maven")
    }

    apply {
        plugin("kotlin")
    }


    extra["junitVersion"] = "5.2.0"
    extra["junitRunnerVersion"] = "1.2.0"
    extra["eclipsePahoVersion"] = "1.2.0"
    extra["gsonVersion"] = "2.8.2"
    extra["ghttpVersion"] = "1.23.0"
    extra["bouncyCastleVersion"] = "1.59"
    extra["orientDBVersion"] = "3.0.7"
    extra["jadeVersion"] = "4.5.0"
    extra["jolVersion"] = "0.9"
    extra["h2Version"] = "1.4.197"
    extra["slf4j_version"] = "1.8.0-beta2"
    extra["kotlin_version"] = "1.2.70"
    extra["kotlinlog_version"] = "1.5.4"
    //extra.set("vertxVersion", "3.5.1")


    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}