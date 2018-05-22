
subprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven("http://jade.tilab.com/maven")
    }

    extra.set("junitVersion", "5.2.0")
    extra.set("junitRunnerVersion", "1.2.0")
    extra.set("eclipsePahoVersion", "1.2.0")
    extra.set("gsonVersion", "2.8.2")
    extra.set("bouncyCastleVersion", "1.59")
    extra.set("jadeVersion", "4.5.0")
    extra.set("jolVersion", "0.9")
    extra.set("h2Version", "1.4.197")
    extra.set("hibernateVersion", "5.3.0.Final")
    extra.set("hibernateValidatorVersion", "6.0.10.Final")
        //set("vertxVersion", "3.5.1")

}

plugins {
    base
}
