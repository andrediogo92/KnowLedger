package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.db.OrientDB
import org.knowledger.ledger.database.ManagedDatabase
import org.knowledger.ledger.database.ManagedSession

class OrientDatabase(
    val dbInfo: OrientDatabaseInfo,
) : ManagedDatabase {
    private val instance: OrientDB =
        OrientDB("${dbInfo.databaseMode.mode}:${dbInfo.path}", dbInfo.config)

    override fun newManagedSession(dbName: String): ManagedSession =
        OrientSession(instance, dbName, dbInfo)

    override fun close() {
        instance.close()
    }


}