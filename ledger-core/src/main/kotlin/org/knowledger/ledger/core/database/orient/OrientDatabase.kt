package org.knowledger.ledger.core.database.orient

import com.orientechnologies.orient.core.db.OrientDB
import org.knowledger.ledger.core.database.ManagedDatabase
import org.knowledger.ledger.core.database.ManagedSession

class OrientDatabase(
    val dbInfo: OrientDatabaseInfo
) : ManagedDatabase {
    internal val instance: OrientDB =
        OrientDB(
            "${dbInfo.databaseMode.mode}:${dbInfo.path}",
            dbInfo.config
        )

    override fun newManagedSession(dbName: String): ManagedSession =
        OrientSession(
            instance,
            dbName,
            dbInfo
        )

    override fun close() {
        instance.close()
    }


}