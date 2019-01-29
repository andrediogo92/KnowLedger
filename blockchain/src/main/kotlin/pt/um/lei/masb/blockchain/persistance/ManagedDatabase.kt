package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDB

interface ManagedDatabase {
    val instance: OrientDB

    fun newManagedSession(
        dbSessionInfo: ManagedSessionInfo
    ): ManagedSession

    fun newManagedSession(): ManagedSession
}