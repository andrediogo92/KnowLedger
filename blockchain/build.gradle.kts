group = "pt.um.lei.masb"
version = "1.0"

plugins {
    `java-library`
}

dependencies {
    //compile("io.vertx", "vertx-mongo-client", vertxVersion)
    compileClasspath("com.h2database", "h2", project.ext["h2Version"] as String)
    compileClasspath("org.hibernate", "hibernate-core", project.ext["hibernateVersion"] as String)
    compile("org.hibernate", "hibernate-validator", project.ext["hibernateValidatorVersion"] as String)
    testCompile("org.junit.jupiter", "junit-jupiter-api", project.ext["junitVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", project.ext["junitVersion"] as String)
    compile("com.google.code.gson", "gson", project.ext["gsonVersion"] as String)
    implementation("org.bouncycastle", "bcprov-jdk15on", project.ext["bouncyCastleVersion"] as String)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}
