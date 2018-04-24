
subprojects {

    group = "pt.um.lei.masb"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        jcenter()
        maven("http://jade.tilab.com/maven")
    }

    ext {
        set("junitVersion", "5.1.0")
        set("gsonVersion", "2.8.2")
        set("bouncyCastleVersion", "1.59")
        set("jadeVersion", "4.5.0")
        set("h2Version", "1.4.197")
        set("hibernateVersion","5.2.16.Final")
        set("hibernateValidatorVersion","6.0.9.Final")
        set("jolVersion", "0.9")
        //set("vertxVersion", "3.5.1")
    }

}

plugins {
    base
}
