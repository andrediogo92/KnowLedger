package org.knowledger.ledger.database.orient

import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.record.OElement
import org.knowledger.ledger.database.ManagedSchemas
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageBytes
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.database.StorageResults
import org.knowledger.ledger.database.query.GenericQuery

data class OrientSession(
    internal val db: OrientDB,
    val dbName: String,
    val dbInfo: OrientDatabaseInfo
) : ManagedSession {
    override fun remove(id: StorageID): StorageID? =
        if (id is DocumentID) {
            session.delete(id.id)
            id
        } else {
            null
        }

    override val isClosed: Boolean
        get() = session.isClosed

    internal var session: ODatabaseDocument =
        openSession()

    override val managedSchemas: ManagedSchemas =
        OrientSchemas(session.metadata.schema)

    val clustersPresent: List<String>
        get() = session.clusterNames.toList()

    val name: String
        get() = session.name


    override fun close() =
        apply {
            session.close()
        }

    override fun makeActive(): ManagedSession =
        apply {
            session.activateOnCurrentThread()
        }

    override fun reOpenIfNecessary() =
        apply {
            if (session.isClosed) {
                session = openSession()
            }
        }

    override fun query(query: GenericQuery): StorageResults =
        DocumentResults(session.query(query.query, query.params))

    override fun query(query: String): StorageResults =
        DocumentResults(session.query(query))

    override fun save(elem: StorageElement): StorageElement? =
        if (elem is DocumentElement) {
            elem.apply {
                session.save<OElement>(elem.elem)
            }
        } else {
            null
        }

    override fun save(
        elem: StorageElement, cluster: String
    ): StorageElement? =
        if (elem is DocumentElement) {
            elem.apply {
                session.save<OElement>(elem.elem, cluster)
            }
        } else {
            null
        }

    override fun newInstance(): StorageElement =
        DocumentElement(session.newElement())

    override fun newInstance(className: String): StorageElement =
        DocumentElement(session.newElement(className))

    override fun newInstance(bytes: ByteArray): StorageBytes =
        DocumentBytes(session.newBlob(bytes))


    override fun begin(): ManagedSession =
        apply {
            session.begin()
        }

    override fun commit(): ManagedSession =
        apply {
            session.commit()
        }

    override fun rollback(): ManagedSession =
        apply {
            session.rollback()
        }


    private fun openSession(): ODatabaseDocument =
        let {
            if (!db.exists(dbName)) {
                db.create(
                    dbName,
                    dbInfo.databaseType.toOType()
                )
            }
            db.open(
                dbName,
                dbInfo.user,
                dbInfo.password
            )
        }

    fun browseClass(clazz: String): List<StorageElement> =
        session.browseClass(clazz).map(::DocumentElement)
}