package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession

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