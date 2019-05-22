package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.db.OrientDB
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedSession

class OrientDatabase(
    val dbInfo: OrientDatabaseInfo
) : ManagedDatabase {
    internal val instance: OrientDB =
        OrientDB(
            "${dbInfo.modeOpenOrient.mode}:${dbInfo.path}",
            dbInfo.config
        )

    override fun newManagedSession(): ManagedSession =
        OrientSession(
            instance,
            dbInfo
        )

    override fun close() {
        instance.close()
    }


}