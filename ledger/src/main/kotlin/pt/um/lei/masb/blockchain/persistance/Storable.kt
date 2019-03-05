package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.record.OElement

interface Storable {
    fun store(
        session: NewInstanceSession
    ): OElement
}