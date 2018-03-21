group = "pt.um.lei.masb"
version = "1.0"

//val vertxVersion = "3.5.1"
val junitversion = "5.1.0"

plugins {
    `java-library`
}

dependencies {
    //compile("io.vertx", "vertx-mongo-client", vertxVersion)
    testCompile("org.junit.jupiter", "junit-jupiter-api", junitversion)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", junitversion)
    compile("com.google.code.gson", "gson", "2.8.2")
    implementation("org.bouncycastle", "bcprov-jdk15on", "1.59")
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}
