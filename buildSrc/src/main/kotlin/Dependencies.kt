object Versions {
    const val assertK = "0.23"
    const val autoService = "1.0-rc7"
    const val bouncyCastle = "1.66"
    const val commonsCodec = "1.15"
    const val commonsRNG = "1.3"
    const val datetime = "0.1.0"
    const val dokka = "1.4.10"
    const val eclipsePaho = "1.2.5"
    const val jade = "4.5.0"
    const val jadeMisc = "2.8.0"
    const val jdk = "11"
    const val jdkV = 11
    const val jmh = "1.26"
    const val jmhPlugin = "0.5.2"
    const val jUnit = "5.7.0"
    const val jUnitRunner = "1.7.0"
    const val kotlinVersion = "1.4.10"
    const val koin = "2.1.6"
    const val kotlinIO = "0.1.16"
    const val kotlinPoet = "1.7.2"
    const val kotlinResult = "1.1.9"
    const val orientDB = "3.1.2"
    const val serial = "1.0.0"
    const val tinylog = "2.1.2"
    const val uuid = "0.2.2"
}

object Libs {
    const val assertK = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}"

    const val autoService = "com.google.auto.service:auto-service:${Versions.autoService}"

    const val bouncyCastle = "org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}"

    const val commonsCodec = "commons-codec:commons-codec:${Versions.commonsCodec}"
    const val commonsRNG = "org.apache.commons:commons-rng-simple:${Versions.commonsRNG}"

    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:${Versions.datetime}"

    const val dokkaPlugin = "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"

    const val eclipsePaho =
        "org.eclipse.paho:org.eclipse.paho.mqttv5.client:${Versions.eclipsePaho}"

    val jade = listOf(
        "com.tilab.jade:jade:${Versions.jade}",
        "com.tilab.jade:jade-misc:${Versions.jadeMisc}"
    )

    const val jmh = "org.openjdk.jmh:jmh-core:${Versions.jmh}"

    const val jmhPlugin = "me.champeau.gradle:jmh-gradle-plugin:${Versions.jmhPlugin}"

    const val jUnitApi = "org.junit.jupiter:junit-jupiter-api:${Versions.jUnit}"


    val jUnitRuntime = listOf(
        "org.junit.jupiter:junit-jupiter-engine:${Versions.jUnit}",
        "org.junit.jupiter:junit-jupiter-params:${Versions.jUnit}",
        "org.junit.platform:junit-platform-runner:${Versions.jUnitRunner}"
    )

    const val koin = "org.koin:koin-core:${Versions.koin}"

    /** Useless until it fully implements Buffers API.
    const val kotlinIO = "org.jetbrains.kotlinx:kotlinx-io:${Versions.kotlinIO}"
    const val kotlinIOJVM = "org.jetbrains.kotlinx:kotlinx-io-jvm:${Versions.kotlinIO}"
     */
    const val kotlinPoet = "com.squareup:kotlinpoet:${Versions.kotlinPoet}"

    const val kotlinResult = "com.michael-bull.kotlin-result:kotlin-result:${Versions.kotlinResult}"

    val orientDB = listOf(
        "com.orientechnologies:orientdb-client:${Versions.orientDB}",
        "com.orientechnologies:orientdb-server:${Versions.orientDB}"
    )

    val serialization = listOf(
        "org.jetbrains.kotlinx:kotlinx-serialization-core:${Versions.serial}",
        "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serial}",
        "org.jetbrains.kotlinx:kotlinx-serialization-cbor:${Versions.serial}"
    )

    val tinylog = listOf(
        "org.tinylog:tinylog-api-kotlin:${Versions.tinylog}",
        "org.tinylog:tinylog-impl:${Versions.tinylog}"
    )

    const val uuid = "com.benasher44:uuid:${Versions.uuid}"

}

object Plugins {
    const val base = "org.knowledger.plugin.base"
    const val serial = "org.knowledger.plugin.serial"
    const val dokka = "org.jetbrains.dokka"
    const val jmh = "me.champeau.gradle.jmh"
    const val noarg = "org.jetbrains.kotlin.plugin.noarg"
    const val serialization = "org.jetbrains.kotlin.plugin.serialization"
}