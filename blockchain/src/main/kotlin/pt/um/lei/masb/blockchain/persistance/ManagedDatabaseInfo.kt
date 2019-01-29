package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDBConfig

data class ManagedDatabaseInfo(
    internal val modeOpen: DatabaseMode = DatabaseMode.EMBEDDED,
    internal val path: String = "./db",
    internal val options: OrientDBConfig =
        OrientDBConfig.defaultConfig()
)