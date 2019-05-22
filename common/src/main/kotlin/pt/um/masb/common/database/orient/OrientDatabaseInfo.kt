package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDBConfig

data class OrientDatabaseInfo(
    internal val modeOpenOrient: OrientDatabaseMode = OrientDatabaseMode.EMBEDDED,
    internal val path: String = "./db",
    internal val options: OrientDBConfig =
        OrientDBConfig.defaultConfig(),
    internal val dbName: String = "ledger",
    internal val user: String = "admin",
    internal val password: String = "admin",
    internal val mode: ODatabaseType = ODatabaseType.PLOCAL
)