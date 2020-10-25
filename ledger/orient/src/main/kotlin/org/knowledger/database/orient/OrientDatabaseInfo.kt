package org.knowledger.database.orient

import com.orientechnologies.orient.core.db.OrientDBConfig
import org.knowledger.ledger.database.DatabaseMode
import org.knowledger.ledger.database.DatabaseType


data class OrientDatabaseInfo(
    internal val databaseMode: DatabaseMode = DatabaseMode.EMBEDDED,
    internal val databaseType: DatabaseType = DatabaseType.LOCAL,
    internal val path: String = "./db",
    internal val user: String = "admin",
    internal val password: String = "admin",
    internal val options: OrientConfig =
        OrientConfig.DEFAULT,
) {
    val config: OrientDBConfig = if (options == OrientConfig.DEFAULT) {
        OrientDBConfig.defaultConfig()
    } else {
        //TODO: Handle custom configuration options.
        TODO("Custom configurations not handled yet")
    }
}