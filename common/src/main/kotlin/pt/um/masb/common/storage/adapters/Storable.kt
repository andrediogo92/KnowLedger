package pt.um.masb.common.storage.adapters

import com.orientechnologies.orient.core.record.OElement
import pt.um.masb.common.database.NewInstanceSession

interface Storable {
    fun store(
        session: NewInstanceSession
    ): OElement
}