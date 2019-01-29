package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.document.ODatabaseDocument

class PluggableSession(
    val db: OrientDB,
    val dbInfo: ManagedSessionInfo
) : ManagedSession {
    private var _session: ODatabaseDocument =
        openSession()


    override val session: ODatabaseDocument
        get() = _session

    override fun reOpenIfNecessary() {
        if (_session.isClosed) {
            _session = openSession()
        }
    }

    private fun openSession(): ODatabaseDocument =
        let {
            if (!db.exists(dbInfo.dbName)) {
                db.create(
                    dbInfo.dbName,
                    dbInfo.mode
                )
            }
            db.open(
                dbInfo.dbName,
                dbInfo.user,
                dbInfo.password
            )
        }


}