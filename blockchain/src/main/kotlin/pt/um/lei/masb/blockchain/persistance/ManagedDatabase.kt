package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.document.ODatabaseDocument

interface ManagedDatabase {
    val instance: OrientDB
    val session: ODatabaseDocument

    fun reOpenIfNecessary()
}