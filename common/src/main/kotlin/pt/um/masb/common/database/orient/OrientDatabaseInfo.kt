package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.db.OrientDBConfig
import pt.um.masb.common.database.DatabaseMode
import pt.um.masb.common.database.DatabaseType


data class OrientDatabaseInfo(
    internal val databaseMode: DatabaseMode = DatabaseMode.EMBEDDED,
    internal val databaseType: DatabaseType = DatabaseType.LOCAL,
    internal val path: String = "./db",
    internal val options: OrientConfig =
        OrientConfig.DEFAULT,
    internal val user: String = "admin",
    internal val password: String = "admin"
) {
    val config: OrientDBConfig = if (options == OrientConfig.DEFAULT) {
        OrientDBConfig.defaultConfig()
    } else {
        //TODO: Handle custom configuration options.
        TODO("Custom configurations not handled yet")
    }
}