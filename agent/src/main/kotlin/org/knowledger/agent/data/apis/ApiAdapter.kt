package org.knowledger.agent.data.apis

import io.ktor.client.HttpClient
import org.knowledger.agent.data.DataSource


interface ApiAdapter {
    suspend fun query(client: HttpClient, dataSource: DataSource): ApiIterator
}
