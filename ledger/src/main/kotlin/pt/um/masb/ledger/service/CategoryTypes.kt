package pt.um.masb.ledger.service

import com.orientechnologies.orient.core.record.OElement
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.LedgerContract

data class CategoryTypes(
    internal val categoryTypes: List<String>
) : Storable, LedgerContract {
    override fun store(session: NewInstanceSession): OElement =
        session.newInstance(
            "CategoryTypes"
        ).apply {
            this.setProperty(
                "categoryTypes",
                categoryTypes
            )
        }
}