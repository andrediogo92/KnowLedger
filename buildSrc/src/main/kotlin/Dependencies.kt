object Versions {
    const val arrowK = "0.8.2"
    const val assertK = "0.13"
    const val bouncyCastle = "1.61"
    const val coroutines = "1.1.1"
    const val eclipsePaho = "1.2.0"
    const val jade = "4.5.0"
    const val jadeMisc = "2.8.0"
    const val jol = "0.9"
    const val jUnit = "5.4.0"
    const val jUnitRunner = "1.4.0"
    const val klog = "1.6.24"
    const val kotlin = "1.3.21"
    const val ktor = "1.1.3"
    const val moshi = "1.8.0"
    const val orientDB = "3.0.14"
    const val slf4j = "1.8.0-beta2"
    const val serial = "0.10.0"
}

object Libs {
    val arrowK =
        "io.arrow-kt:arrow-core:${Versions.arrowK}"

    val assertK =
        "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertK}"

    val bouncyCastle =
        "org.bouncycastle:bcprov-jdk15on:${Versions.bouncyCastle}"

    val coroutines =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"

    val eclipsePaho =
        "org.eclipse.paho:org.eclipse.paho.client.mqttv3:${Versions.eclipsePaho}"

    val jade = listOf(
        "com.tilab.jade:jade:${Versions.jade}",
        "com.tilab.jade:jade-misc:${Versions.jadeMisc}"
    )

    val jUnitApi =
        "org.junit.jupiter:junit-jupiter-api:${Versions.jUnit}"


    val jUnitRuntime = listOf(
        "org.junit.jupiter:junit-jupiter-engine:${Versions.jUnit}",
        "org.junit.jupiter:junit-jupiter-params:${Versions.jUnit}",
        "org.junit.platform:junit-platform-runner:${Versions.jUnitRunner}"
    )

    val klog = "io.github.microutils:kotlin-logging:${Versions.klog}"

    val ktor = listOf(
        "io.ktor:ktor-client-core:${Versions.ktor}",
        "io.ktor:ktor-client-cio:${Versions.ktor}",
        "io.ktor:ktor-client-json:${Versions.ktor}",
        "io.ktor:ktor-client-json-jvm:${Versions.ktor}"
    )

    val jol =
        "org.openjdk.jol:jol-core:${Versions.jol}"

    val moshi = listOf(
        "com.squareup.moshi:moshi:${Versions.moshi}",
        "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    )

    val moshiCodeGen =
        "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"

    val orientDB = listOf(
        "com.orientechnologies:orientdb-client:${Versions.orientDB}",
        "com.orientechnologies:orientdb-server:${Versions.orientDB}"
    )

    val serialization =
        "org.jetbrains.kotlinx:kotlinx-serialization-runtime:${Versions.serial}"


    val slf4j = listOf(
        "org.slf4j:slf4j-api:${Versions.slf4j}",
        "org.slf4j:slf4j-simple:${Versions.slf4j}"
    )

}