package pt.um.lei.masb.blockchain.persistance.database

import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.OBlob

inline class NewInstanceSession(
    private val session: ODatabaseDocument
) {
    fun newInstance(): OElement =
        session.newElement()

    fun newInstance(className: String): OElement =
        session.newElement(className)

    fun newInstance(bytes: ByteArray): OBlob =
        session.newBlob(bytes)
}