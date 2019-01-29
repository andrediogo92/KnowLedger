package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.ODatabaseType

data class ManagedSessionInfo(
    internal val dbName: String = "blockchain",
    internal val user: String = "admin",
    internal val password: String = "admin",
    internal val mode: ODatabaseType = ODatabaseType.PLOCAL
)