group = "pt.um.lei.masb"
version = "1.0"

//val vertxVersion = "3.5.1"
val junitversion = "5.1.0"
val gsonversion = "2.8.2"
val bouncycastle = "1.59"

plugins {
    `java-library`
}

dependencies {
    //compile("io.vertx", "vertx-mongo-client", vertxVersion)
    testCompile("org.junit.jupiter", "junit-jupiter-api", junitversion)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", junitversion)
    compile("com.google.code.gson", "gson", gsonversion)
    implementation("org.bouncycastle", "bcprov-jdk15on", bouncycastle)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}
