package pt.um.lei.masb.blockchain.persistance.results

import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.results.Failable

sealed class DataListResult<T : BlockChainData> {
    data class Success<T : BlockChainData>(
        val data: List<T>
    ) : DataListResult<T>()

    data class QueryFailure<T : BlockChainData>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, DataListResult<T>()

    data class NonExistentData<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataListResult<T>()

    data class UnrecognizedDataType<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataListResult<T>()

    data class UnrecognizedUnit<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataListResult<T>()

    data class UnexpectedClass<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataListResult<T>()

    data class NonRegisteredSchema<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataListResult<T>()

    data class Propagated<T : BlockChainData>(
        val pointOfFailure: String,
        val failable: Failable
    ) : Failable, DataListResult<T>() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }

}