group = "pt.um.lei.masb"
version = "1.0"

plugins {
    `java-library`
}

dependencies {
    //compile("io.vertx", "vertx-mongo-client", vertxVersion)
    testCompile("org.junit.jupiter", "junit-jupiter-api", project.extra["junitVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", project.extra["junitVersion"] as String)
    compile("com.google.code.gson", "gson", project.extra["gsonVersion"] as String)
    compile("org.hibernate.validator", "hibernate-validator", project.extra["hibernateValidatorVersion"] as String)
    implementation("org.bouncycastle", "bcprov-jdk15on", project.extra["bouncyCastleVersion"] as String)
    implementation("org.openjdk.jol", "jol-core", project.extra["jolVersion"] as String)
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}
