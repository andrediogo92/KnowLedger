package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.db.OrientDB
import com.orientechnologies.orient.core.db.document.ODatabaseDocument

class PluggableDatabase(
    dbInfo: ManagedDatabaseInfo
) : ManagedDatabase {
    val dbName = dbInfo.dbName
    val dbUser = dbInfo.user
    val dbPassword = dbInfo.password
    val dbMode = dbInfo.mode

    override val instance: OrientDB =
        OrientDB(
            "${dbInfo.modeOpen.mode}:${dbInfo.path}",
            dbInfo.options
        )

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
            if (!instance.exists(dbName)) {
                instance.create(
                    dbName,
                    dbMode
                )
            }
            instance.open(
                dbName,
                dbUser,
                dbPassword
            )
        }


}