package pt.um.masb.agent.net

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.util.KtorExperimentalAPI
import org.tinylog.kotlin.Logger
import pt.um.masb.agent.data.DataSource
import pt.um.masb.agent.data.apis.ApiAdapter
import kotlin.collections.set
import kotlin.random.Random

class HTTPRunner {

    @KtorExperimentalAPI
    private val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    private val apis: MutableList<DataSource> = mutableListOf()
    private val matchers: MutableMap<String, ApiAdapter> = mutableMapOf()


    fun registerMatcher(toMatch: String, toDeserialize: ApiAdapter) {
        matchers[toMatch] = toDeserialize
    }

    fun registerMatchers(matchers: Map<String, ApiAdapter>) {
        this.matchers.putAll(matchers)
    }


    fun registerSources(apis: List<DataSource>) {
        this.apis.addAll(apis)
    }

    suspend fun runRandom() =
        runApiQuery(Random.nextInt(apis.size))


    fun registeredApis(): Int = apis.size

    suspend fun run(i: Int) =
        if (i < registeredApis() && i > 0) {
            runApiQuery(i)
        } else {
            Logger.error(IndexOutOfBoundsException())
            null
        }

    private suspend fun runApiQuery(i: Int) =
        matchers[apis[i].id]?.query(client, apis[i])
}
