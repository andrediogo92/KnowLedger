package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.db.OrientDBConfig


data class OrientDatabaseInfo(
    internal val modeOpenOrient: OrientDatabaseMode = OrientDatabaseMode.EMBEDDED,
    internal val path: String = "./db",
    internal val options: OrientConfig =
        OrientConfig.DEFAULT,
    internal val dbName: String = "ledger",
    internal val user: String = "admin",
    internal val password: String = "admin",
    internal val mode: OrientDatabaseType = OrientDatabaseType.LOCAL
) {
    val config: OrientDBConfig = if (options == OrientConfig.DEFAULT) {
        OrientDBConfig.defaultConfig()
    } else {
        //TODO: Handle custom configuration options.
        TODO("Custom configurations not handled yet")
    }
}