object Versions {
    val bouncyCastle = "1.59"
    val coroutines = "1.0.1"
    val eclipsePaho = "1.2.0"
    val jade = "4.5.0"
    val jadeMisc = "2.8.0"
    val jol = "0.9"
    val jUnit = "5.2.0"
    val jUnitRunner = "1.2.0"
    val klog = "1.5.4"
    val kotlin = "1.3.11"
    val ktor = "1.0.0"
    val orientDB = "3.0.7"
    val slf4j = "1.8.0-beta2"
    val serial = "0.9.1"
}

object Libs {


    val bouncyCastle =
        mapOf(
            "group" to "org.bouncycastle",
            "name" to "bcprov-jdk15on",
            "version" to Versions.bouncyCastle
        )


    val coroutines =
        mapOf(
            "group" to "org.jetbrains.kotlinx",
            "name" to "kotlinx-coroutines-core",
            "version" to Versions.coroutines
        )


    val eclipsePaho = mapOf(
        "group" to "org.eclipse.paho",
        "name" to "org.eclipse.paho.client.mqttv3",
        "version" to Versions.eclipsePaho
    )


    val jade = listOf(
        mapOf(
            "group" to "com.tilab.jade",
            "name" to "jade",
            "version" to Versions.jade
        ),
        mapOf(
            "group" to "com.tilab.jade",
            "name" to "jade-misc",
            "version" to Versions.jadeMisc
        )
    )


    val jUnitApi = mapOf(
        "group" to "org.junit.jupiter",
        "name" to "junit-jupiter-api",
        "version" to Versions.jUnit
    )


    val jUnitRuntime = listOf(
        mapOf(
            "group" to "org.junit.jupiter",
            "name" to "junit-jupiter-engine",
            "version" to Versions.jUnit
        ),
        mapOf(
            "group" to "org.junit.jupiter",
            "name" to "junit-jupiter-params",
            "version" to Versions.jUnit
        ),
        mapOf(
            "group" to "org.junit.platform",
            "name" to "junit-platform-runner",
            "version" to Versions.jUnitRunner
        )
    )


    val klog = mapOf(
        "group" to "io.github.microutils",
        "name" to "kotlin-logging",
        "version" to Versions.klog
    )


    val ktor = listOf(
        mapOf(
            "group" to "io.ktor",
            "name" to "ktor-client-core",
            "version" to Versions.ktor
        ),
        mapOf(
            "group" to "io.ktor",
            "name" to "ktor-client-cio",
            "version" to Versions.ktor
        ),
        mapOf(
            "group" to "io.ktor",
            "name" to "ktor-client-json",
            "version" to Versions.ktor
        ),
        mapOf(
            "group" to "io.ktor",
            "name" to "ktor-client-json-jvm",
            "version" to Versions.ktor
        )
    )


    val jol =
        mapOf(
            "group" to "org.openjdk.jol",
            "name" to "jol-core",
            "version" to Versions.jol
        )


    val serialization =
        mapOf(
            "group" to "org.jetbrains.kotlinx",
            "name" to "kotlinx-serialization-runtime",
            "version" to Versions.serial
        )

    val serializationModule =
        mapOf(
            "group" to "org.jetbrains.kotlin",
            "name" to "kotlin-serialization",
            "version" to Versions.kotlin
        )


    val slf4j = listOf(
        mapOf(
            "group" to "org.slf4j",
            "name" to "slf4j-api",
            "version" to Versions.slf4j
        ),
        mapOf(
            "group" to "org.slf4j",
            "name" to "slf4j-simple",
            "version" to Versions.slf4j
        )
    )


    val orientDB = listOf(
        mapOf(
            "group" to "com.orientechnologies",
            "name" to "orientdb-client",
            "version" to Versions.orientDB
        ),
        mapOf(
            "group" to "com.orientechnologies",
            "name" to "orientdb-commons",
            "version" to Versions.orientDB
        ),
        mapOf(
            "group" to "com.orientechnologies",
            "name" to "orientdb-enterprise",
            "version" to Versions.orientDB
        ),
        mapOf(
            "group" to "com.orientechnologies",
            "name" to "orientdb-nativeos",
            "version" to Versions.orientDB
        ),
        mapOf(
            "group" to "com.orientechnologies",
            "name" to "orientdb-server",
            "version" to Versions.orientDB
        )
    )
}