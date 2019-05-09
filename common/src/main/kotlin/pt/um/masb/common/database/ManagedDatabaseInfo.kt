package pt.um.masb.common.database

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDBConfig

data class ManagedDatabaseInfo(
    internal val modeOpen: DatabaseMode = DatabaseMode.EMBEDDED,
    internal val path: String = "./db",
    internal val options: OrientDBConfig =
        OrientDBConfig.defaultConfig(),
    internal val dbName: String = "ledger",
    internal val user: String = "admin",
    internal val password: String = "admin",
    internal val mode: ODatabaseType = ODatabaseType.PLOCAL
)