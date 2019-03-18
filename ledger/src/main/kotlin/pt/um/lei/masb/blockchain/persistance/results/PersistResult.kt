package pt.um.lei.masb.blockchain.persistance.results

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.utils.Failable

sealed class PersistResult {
    data class Success(
        val data: OElement
    ) : PersistResult()

    data class QueryFailure(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, PersistResult()

    data class NonExistentData(
        override val cause: String
    ) : Failable, PersistResult()

    data class UnrecognizedDataType(
        override val cause: String
    ) : Failable, PersistResult()

    data class UnrecognizedUnit(
        override val cause: String
    ) : Failable, PersistResult()

    data class UnexpectedClass(
        override val cause: String
    ) : Failable, PersistResult()

    data class NonRegisteredSchema(
        override val cause: String
    ) : Failable, PersistResult()

    data class NonMatchingCrypter(
        override val cause: String
    ) : Failable, PersistResult()

    data class UnregisteredCrypter(
        override val cause: String
    ) : Failable, PersistResult()
}