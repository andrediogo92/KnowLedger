plugins {
    `java-library`
}

dependencies {
    implementation("com.h2database", "h2", project.extra["h2Version"] as String)
    implementation("org.hibernate", "hibernate-core", project.extra["hibernateVersion"] as String)
    testRuntime("org.junit.platform", "junit-platform-runner", project.extra["junitRunnerVersion"] as String)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", project.extra["junitVersion"] as String)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", project.extra["junitVersion"] as String)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-params", project.extra["junitVersion"] as String)
    implementation("com.google.code.gson", "gson", project.extra["gsonVersion"] as String)
    implementation("org.hibernate.validator", "hibernate-validator", project.extra["hibernateValidatorVersion"] as String)
    implementation("org.bouncycastle", "bcprov-jdk15on", project.extra["bouncyCastleVersion"] as String)
    implementation("org.openjdk.jol", "jol-core", project.extra["jolVersion"] as String)
    implementation("org.slf4j", "slf4j-api", extra["slf4j_version"] as String)
    runtime("org.slf4j", "slf4j-simple", extra["slf4j_version"] as String)
}


tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}

tasks.withType<JavaExec> {
    jvmArgs("-Djdk.attach.allowAttachSelf=true")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}
