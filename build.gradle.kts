
subprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven("http://jade.tilab.com/maven")
    }

    extra.set("junitVersion", "5.1.0")
    extra.set("gsonVersion", "2.8.2")
    extra.set("bouncyCastleVersion", "1.59")
    extra.set("jadeVersion", "4.5.0")
    extra.set("jolVersion", "0.9")
        //set("vertxVersion", "3.5.1")

}

plugins {
    base
}
