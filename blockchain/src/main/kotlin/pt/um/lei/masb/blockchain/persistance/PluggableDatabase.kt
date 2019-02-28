package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDB

class PluggableDatabase(val dbInfo: ManagedDatabaseInfo) : ManagedDatabase {
    override fun newManagedSession(): ManagedSession =
        PluggableSession(
            instance,
            dbInfo
        )

    override val instance: OrientDB =
        OrientDB(
            "${dbInfo.modeOpen.mode}:${dbInfo.path}",
            dbInfo.options
        )

}