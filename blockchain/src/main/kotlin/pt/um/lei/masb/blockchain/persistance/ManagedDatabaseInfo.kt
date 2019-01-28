package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDBConfig

data class ManagedDatabaseInfo(
    internal val modeOpen: DatabaseMode = DatabaseMode.MEMORY,
    internal val mode: ODatabaseType = ODatabaseType.MEMORY,
    internal val path: String = "./db",
    internal val options: OrientDBConfig =
        OrientDBConfig.defaultConfig(),
    internal val dbName: String = "blockchain",
    internal val user: String = "admin",
    internal val password: String = "admin"
)