package pt.um.masb.common.database

import com.orientechnologies.orient.core.db.OrientDB

interface ManagedDatabase {
    val instance: OrientDB

    fun newManagedSession(): ManagedSession
}