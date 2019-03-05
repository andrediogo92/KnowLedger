package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.document.ODatabaseDocument

interface ManagedSession {
    val session: ODatabaseDocument
    fun reOpenIfNecessary()
}