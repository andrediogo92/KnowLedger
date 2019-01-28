package pt.um.lei.masb.agent.net

import kotlinx.serialization.Serializable
import mu.KLogging
import pt.um.lei.masb.agent.Container
import pt.um.lei.masb.agent.data.DataSource
import java.io.File
import java.io.FileReader
import java.io.InputStreamReader
import java.net.URI


@Serializable
data class JSONMappedURLs(
    val apis: List<DataSource> = emptyList(),
    val tcp: List<DataSource> = emptyList(),
    val jade: List<DataSource> = emptyList()
) {
    companion object : KLogging() {
        val apiScheme = Regex.fromLiteral("http(s)?")
        val tcpScheme = Regex.fromLiteral("tcp")
        val jadeScheme = Regex.fromLiteral("jade")

        /**
         * A parser for a file based on a path string from the classpath resources.
         *
         * @param file the path string to a resource in the classpath.
         */
        fun jsonFileUrlParser(file: String): JSONMappedURLs =
            InputStreamReader(
                Container::class.java
                    .getResourceAsStream(file)
            ).use {
                //JSON.parse(this.serializer(), it.readText())
                TODO()
            }

        /**
         * A parser for a file at the specified URI.
         *
         * @param file
         */
        fun jsonFileUrlParser(file: URI): JSONMappedURLs =
            FileReader(File(file)).use {
                //JSON.parse(this.serializer(), it.readText())
                TODO()
            }


    }


}
