object Versions {
    const val arrowK = "0.9"
    const val assertK = "0.17"
    const val autoService = "1.0-rc5"
    const val bouncyCastle = "1.61"
    const val commons = "1.2"
    const val coroutines = "1.3.0-M1"
    const val dokka = "0.9.18"
    const val eclipsePaho = "1.2.0"
    const val jade = "4.5.0"
    const val jadeMisc = "2.8.0"
    const val jol = "0.9"
    const val jUnit = "5.4.2"
    const val jUnitRunner = "1.4.2"
    const val kotlin = "1.3.41"
    const val koin = "2.0.1"
    const val ktor = "1.2.2"
    const val moshi = "1.8.0"
    const val kotlinPoet = "1.2.0"
    const val orientDB = "3.0.14"
    const val serial = "0.11.1"
    const val tinylog = "2.0.0-M4.3"

    /*
    * Deprecate old logging methods,
    * for tinylog.
    *
    const val slf4j = "1.8.0-beta2"
    const val klog = "1.6.26"
    */
}

object Libs {
    const val arrowK =
        "io.arrow-kt:arrow-core:${Versions.arrowK}"

    const val assertK =
        "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}"

    const val autoService =
        "com.google.auto.service:auto-service:${Versions.autoService}"

    const val bouncyCastle =
        "org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}"

    const val commonsRNG = "org.apache.commons:commons-rng-simple:${Versions.commons}"

    const val coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"

    const val dokkaPlugin =
        "org.jetbrains.dokka:dokka-gradle-plugin:${Versions.dokka}"

    const val eclipsePaho =
        "org.eclipse.paho:org.eclipse.paho.client.mqttv3:${Versions.eclipsePaho}"

    val jade = listOf(
        "com.tilab.jade:jade:${Versions.jade}",
        "com.tilab.jade:jade-misc:${Versions.jadeMisc}"
    )

    const val jUnitApi =
        "org.junit.jupiter:junit-jupiter-api:${Versions.jUnit}"


    val jUnitRuntime = listOf(
        "org.junit.jupiter:junit-jupiter-engine:${Versions.jUnit}",
        "org.junit.jupiter:junit-jupiter-params:${Versions.jUnit}",
        "org.junit.platform:junit-platform-runner:${Versions.jUnitRunner}"
    )

    val ktor = listOf(
        "io.ktor:ktor-client-core:${Versions.ktor}",
        "io.ktor:ktor-client-cio:${Versions.ktor}",
        "io.ktor:ktor-client-json:${Versions.ktor}",
        "io.ktor:ktor-client-json-jvm:${Versions.ktor}"
    )

    const val koin = "org.koin:koin-core:${Versions.koin}"


    const val jol =
        "org.openjdk.jol:jol-core:${Versions.jol}"

    val moshi = listOf(
        "com.squareup.moshi:moshi:${Versions.moshi}",
        "com.squareup.moshi:moshi-adapters:${Versions.moshi}"//,
//        "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    )

    const val moshiCodeGen =
        "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"

    val orientDB = listOf(
        "com.orientechnologies:orientdb-client:${Versions.orientDB}",
        "com.orientechnologies:orientdb-server:${Versions.orientDB}"
    )

    const val kotlinPoet =
        "com.squareup:kotlinpoet:${Versions.kotlinPoet}"

    const val serialization =
        "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.serial}"

    val tinylog = listOf(
        "org.tinylog:tinylog-api-kotlin:${Versions.tinylog}",
        "org.tinylog:tinylog-impl:${Versions.tinylog}"
    )

    /*
    * Deprecate old logging methods,
    * for tinylog.
    *
    const val klog = "io.github.microutils:kotlin-logging:${Versions.klog}"
    val slf4j = listOf(
        "org.slf4j:slf4j-api:${Versions.slf4j}",
        "org.slf4j:slf4j-simple:${Versions.slf4j}"
    )
    */

}