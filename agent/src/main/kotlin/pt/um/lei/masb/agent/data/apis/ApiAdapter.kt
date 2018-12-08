package pt.um.lei.masb.agent.data.apis

import io.ktor.client.HttpClient
import pt.um.lei.masb.agent.data.DataSource


interface ApiAdapter {
    suspend fun query(client: HttpClient, dataSource: DataSource): ApiIterator
}
