
dependencies {
    implementation(kotlin("reflect", project.extra["kotlin_version"] as String))
    implementation(kotlin("stdlib", project.extra["kotlin_version"] as String))
    implementation("com.orientechnologies", "orientdb-client", project.extra["orientDBVersion"] as String)
    implementation("com.orientechnologies", "orientdb-core", project.extra["orientDBVersion"] as String)
    implementation("com.orientechnologies", "orientdb-commons", project.extra["orientDBVersion"] as String)
    implementation("com.orientechnologies", "orientdb-enterprise", project.extra["orientDBVersion"] as String)
    implementation("com.orientechnologies", "orientdb-nativeos", project.extra["orientDBVersion"] as String)
    implementation("com.orientechnologies", "orientdb-server", project.extra["orientDBVersion"] as String)
    testRuntime("org.junit.platform", "junit-platform-runner", project.extra["junitRunnerVersion"] as String)
    testImplementation("org.junit.jupiter", "junit-jupiter-api", project.extra["junitVersion"] as String)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-engine", project.extra["junitVersion"] as String)
    testRuntimeOnly("org.junit.jupiter", "junit-jupiter-params", project.extra["junitVersion"] as String)
    implementation("com.google.code.gson", "gson", project.extra["gsonVersion"] as String)
    implementation("io.github.microutils", "kotlin-logging", extra["kotlinlog_version"] as String)
    implementation("org.bouncycastle", "bcprov-jdk15on", project.extra["bouncyCastleVersion"] as String)
    implementation("org.openjdk.jol", "jol-core", project.extra["jolVersion"] as String)
    implementation("org.slf4j", "slf4j-api", extra["slf4j_version"] as String)
    runtime("org.slf4j", "slf4j-simple", extra["slf4j_version"] as String)
}


tasks.withType<JavaExec> {
    jvmArgs("-Djdk.attach.allowAttachSelf=true")
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

repositories {
    mavenCentral()
}

