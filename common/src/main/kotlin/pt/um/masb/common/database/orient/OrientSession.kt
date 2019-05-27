package pt.um.masb.common.database.orient

import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import com.orientechnologies.orient.core.record.OElement
import pt.um.masb.common.database.ManagedSchemas
import pt.um.masb.common.database.ManagedSession
import pt.um.masb.common.database.StorageBytes
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageResults

data class OrientSession(
    internal val db: OrientDB,
    val dbName: String,
    val dbInfo: OrientDatabaseInfo
) : ManagedSession {
    override val isClosed: Boolean
        get() = session.isClosed

    internal var session: ODatabaseDocument =
        openSession()

    override fun makeActive() {
        session.activateOnCurrentThread()
    }

    override fun save(elem: StorageElement): StorageElement? =
        if (elem is DocumentElement) {
            elem.apply {
                session.save<OElement>(elem.elem)
            }
        } else {
            null
        }

    override fun save(elem: StorageElement, cluster: String): StorageElement? =
        if (elem is DocumentElement) {
            elem.apply {
                session.save<OElement>(elem.elem, cluster)
            }
        } else {
            null
        }

    override fun query(query: String, params: Map<String, Any>): StorageResults =
        DocumentResults(session.query(query, params))

    override fun query(query: String): StorageResults =
        DocumentResults(session.query(query))


    override val managedSchemas: ManagedSchemas =
        OrientSchemas(session.metadata.schema)

    override fun close() = session.close()

    override fun newInstance(): StorageElement =
        DocumentElement(session.newElement())

    override fun newInstance(className: String): StorageElement =
        DocumentElement(session.newElement(className))

    override fun newInstance(bytes: ByteArray): StorageBytes =
        DocumentBytes(session.newBlob(bytes))


    override fun reOpenIfNecessary() {
        if (session.isClosed) {
            session = openSession()
        }
    }

    val clustersPresent: List<String>
        get() = session.clusterNames.toMutableList()

    val name: String
        get() = session.name

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
        session.browseClass(clazz).toList().map {
            DocumentElement(it)
        }


}