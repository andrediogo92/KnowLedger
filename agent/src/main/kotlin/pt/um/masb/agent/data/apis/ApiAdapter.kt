package pt.um.masb.agent.data.apis

import io.ktor.client.HttpClient
import pt.um.masb.agent.data.DataSource


interface ApiAdapter {
    suspend fun query(client: HttpClient, dataSource: DataSource): ApiIterator
}
