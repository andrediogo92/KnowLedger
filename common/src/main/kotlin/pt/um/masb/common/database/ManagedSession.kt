package pt.um.masb.common.database

import com.orientechnologies.orient.core.db.document.ODatabaseDocument

interface ManagedSession {
    val session: ODatabaseDocument
    fun reOpenIfNecessary()
}