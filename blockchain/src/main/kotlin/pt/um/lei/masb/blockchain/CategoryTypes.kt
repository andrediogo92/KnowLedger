package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable

data class CategoryTypes(
    internal val categoryTypes: List<String>
) : Storable, BlockChainContract {
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