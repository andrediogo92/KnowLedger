package pt.um.masb.agent.data.apis

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import pt.um.masb.agent.data.DataSource
import pt.um.masb.ledger.storage.Transaction

open class DarkSkyAdapter : ApiAdapter {
    private lateinit var source: DataSource

    override suspend fun query(client: HttpClient, dataSource: DataSource): ApiIterator =
        DarkSkyIterator(client.get(dataSource.uri.toString()))


    init {
        //Check DataSource supplied matches API
        if (!source.uri.host.contains(r))
            throw IncompatibleAdapterException()
    }

    class DarkSkyIterator(val json: DarkSkyJSON) : ApiIterator {
        override val transactions: Collection<Transaction>
            get() = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.


        override fun hasNext(): Boolean {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun next(): Transaction {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


    }

    class DarkSkyJSON


    class IncompatibleAdapterException : Throwable(message = "Source is not DarkSky!")

    companion object {
        val r: Regex = Regex.fromLiteral("darsky.net")
    }


}
