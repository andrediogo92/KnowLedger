package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.ODatabaseType
import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.OrientDBConfig
import com.orientechnologies.orient.core.db.document.ODatabaseDocument
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal object BlockChainDatabase : ManagedDatabase {

    override val session: ODatabaseDocument
        get() = internalSession

    override val instance: OrientDB by lazy {
        OrientDB(
            "${DatabaseMode.EMBEDDED.mode}:./db",
            OrientDBConfig.defaultConfig()
        )
    }

    private var internalSession: ODatabaseDocument
            by LazyDatabase()

    class LazyDatabase :
        ReadWriteProperty<BlockChainDatabase, ODatabaseDocument> {
        var session: ODatabaseDocument? = null

        override fun getValue(
            thisRef: BlockChainDatabase,
            property: KProperty<*>
        ): ODatabaseDocument =
            if (session == null) {
                session = thisRef.openSession()
                session as ODatabaseDocument
            } else {
                session as ODatabaseDocument
            }


        override fun setValue(
            thisRef: BlockChainDatabase,
            property: KProperty<*>,
            value: ODatabaseDocument
        ) {
            if (session != null) {
                session!!.close()
            }
            session = value
        }
    }

    private fun openSession(): ODatabaseDocument =
        let {
            if (!instance.exists("blockchain")) {
                instance.create(
                    "blockchain",
                    ODatabaseType.PLOCAL
                )
            }
            instance.open(
                "blockchain",
                "admin",
                "admin"
            )
        }


    override fun reOpenIfNecessary() {
        if (session.isClosed) {
            internalSession = openSession()
        }
    }

}

