subprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven("http://jade.tilab.com/maven")
    }

    extra["junitVersion"] = "5.2.0"
    extra["junitRunnerVersion"] = "1.2.0"
    extra["eclipsePahoVersion"] = "1.2.0"
    extra["gsonVersion"] = "2.8.2"
    extra["ghttpVersion"] = "1.23.0"
    extra["bouncyCastleVersion"] = "1.59"
    extra["jadeVersion"] = "4.5.0"
    extra["jolVersion"] = "0.9"
    extra["h2Version"] = "1.4.197"
    extra["hibernateVersion"] = "5.3.0.Final"
    extra["hibernateValidatorVersion"] = "6.0.10.Final"
    extra["slf4j_version"] = "1.8.0-beta2"
    //extra.set("vertxVersion", "3.5.1")

}

plugins {
    base
}
