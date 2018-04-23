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
    compileClasspath("com.h2database", "h2", h2version)
    compileClasspath("org.hibernate", "hibernate-core", hibernatecore)
    compile("org.hibernate", "hibernate-validator", hibernatevalidator)
    implementation("com.google.code.gson", "gson", gsonversion)
    implementation("org.bouncycastle", "bcprov-jdk15on", bouncycastle)
    testCompile("org.junit.jupiter", "junit-jupiter-api", project.ext["junitVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", project.ext["junitVersion"] as String)
    compile("com.google.code.gson", "gson", project.ext["gsonVersion"] as String)
    implementation("org.bouncycastle", "bcprov-jdk15on", project.ext["bouncyCastleVersion"] as String)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}
