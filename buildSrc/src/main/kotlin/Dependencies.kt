object Versions {
    const val assertK = "0.21"
    const val autoService = "1.0-rc6"
    const val bouncyCastle = "1.64"
    const val commons = "1.2"
    const val dokka = "0.10.1"
    const val eclipsePaho = "1.2.0"
    const val jade = "4.5.0"
    const val jadeMisc = "2.8.0"
    const val jdk = "1.8"
    const val jdkV = 8
    const val jmh = "1.21"
    const val jmhPlugin = "0.5.0"
    const val jUnit = "5.6.0"
    const val jUnitRunner = "1.6.0"
    const val kotlin = "1.3.70"
    const val koin = "2.1.1"
    const val kotlinPoet = "1.5.0"
    const val orientDB = "3.0.28"
    const val serial = "0.20.0"
    const val tinylog = "2.0.1"
}

object Libs {
    const val assertK =
        "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}"

    const val autoService =
        "com.google.auto.service:auto-service:${Versions.autoService}"

    const val bouncyCastle =
        "org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}"

    const val commonsRNG = "org.apache.commons:commons-rng-simple:${Versions.commons}"

    const val dokkaPlugin =
        "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"

    const val eclipsePaho =
        "org.eclipse.paho:org.eclipse.paho.client.mqttv3:${Versions.eclipsePaho}"

    val jade = listOf(
        "com.tilab.jade:jade:${Versions.jade}",
        "com.tilab.jade:jade-misc:${Versions.jadeMisc}"
    )

    const val jmh = "org.openjdk.jmh:jmh-core:${Versions.jmh}"

    const val jmhPlugin =
        "me.champeau.gradle:jmh-gradle-plugin:${Versions.jmhPlugin}"

    const val jUnitApi =
        "org.junit.jupiter:junit-jupiter-api:${Versions.jUnit}"


    val jUnitRuntime = listOf(
        "org.junit.jupiter:junit-jupiter-engine:${Versions.jUnit}",
        "org.junit.jupiter:junit-jupiter-params:${Versions.jUnit}",
        "org.junit.platform:junit-platform-runner:${Versions.jUnitRunner}"
    )

    const val koin = "org.koin:koin-core:${Versions.koin}"

    const val kotlinPoet =
        "com.squareup:kotlinpoet:${Versions.kotlinPoet}"

    val orientDB = listOf(
        "com.orientechnologies:orientdb-client:${Versions.orientDB}",
        "com.orientechnologies:orientdb-server:${Versions.orientDB}"
    )

    val serialization = listOf(
        "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.serial}",
        "org.jetbrains.kotlinx:kotlinx-serialization-cbor:${Versions.serial}"
    )

    val tinylog = listOf(
        "org.tinylog:tinylog-api-kotlin:${Versions.tinylog}",
        "org.tinylog:tinylog-impl:${Versions.tinylog}"
    )

}

object Plugins {
    const val base = "org.knowledger.plugin.base"
    const val docs = "org.knowledger.plugin.docs"
    const val serial = "org.knowledger.plugin.serial"
    const val dokka = "org.jetbrains.dokka"
    const val jmh = "me.champeau.gradle.jmh"
    const val noarg = "org.jetbrains.kotlin.plugin.noarg"
    const val serialization = "org.jetbrains.kotlin.plugin.serialization"
}