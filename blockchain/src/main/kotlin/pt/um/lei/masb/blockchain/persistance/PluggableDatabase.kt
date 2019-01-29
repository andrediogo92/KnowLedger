package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDB

class PluggableDatabase(dbInfo: ManagedDatabaseInfo) : ManagedDatabase {
    override fun newManagedSession(): ManagedSession =
        PluggableSession(
            instance,
            ManagedSessionInfo()
        )

    override fun newManagedSession(
        dbSessionInfo: ManagedSessionInfo
    ): ManagedSession =
        PluggableSession(
            instance,
            dbSessionInfo
        )


    override val instance: OrientDB =
        OrientDB(
            "${dbInfo.modeOpen.mode}:${dbInfo.path}",
            dbInfo.options
        )

}