plugins {
    java
    application
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter-api", project.extra["junitVersion"] as String)
    testRuntime("org.junit.platform", "junit-platform-runner", project.extra["junitRunnerVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-params", project.extra["junitVersion"] as String)
    testRuntime("org.junit.jupiter", "junit-jupiter-engine", project.extra["junitVersion"] as String)
    implementation("org.eclipse.paho", "org.eclipse.paho.client.mqttv3", project.extra["eclipsePahoVersion"] as String)
    implementation("com.google.code.gson", "gson", project.extra["gsonVersion"] as String)
    implementation("com.google.http-client", "google-http-client", project.extra["ghttpVersion"] as String)
    implementation("com.google.http-client", "google-http-client-gson", project.extra["ghttpVersion"] as String)
    implementation("org.hibernate.validator", "hibernate-validator", project.extra["hibernateValidatorVersion"] as String)
    implementation("com.tilab.jade", "jade", project.extra["jadeVersion"] as String)
    implementation("com.tilab.jade", "jade-misc", "2.8.0")
    implementation("com.tilab.jade", "jade-test-suite", "1.13.0")
    implementation("org.slf4j", "slf4j-api", extra["slf4j_version"] as String)
    runtime("org.slf4j", "slf4j-simple", extra["slf4j_version"] as String)
    implementation(project(":blockchain"))
}


tasks.withType<JavaCompile> {
    sourceCompatibility = "1.10"
    targetCompatibility = "1.10"
}


configure<ApplicationPluginConvention> {
    mainClassName = "pt.um.lei.masb.agent.Container"
}

tasks.withType<Test> {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}