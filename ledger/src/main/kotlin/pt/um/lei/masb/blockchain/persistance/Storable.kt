package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession

interface Storable {
    fun store(
        session: NewInstanceSession
    ): OElement
}