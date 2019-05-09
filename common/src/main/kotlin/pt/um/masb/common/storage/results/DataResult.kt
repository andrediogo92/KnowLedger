package pt.um.masb.common.storage.results

import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.results.Failable


sealed class DataResult<T : BlockChainData> {
    data class Success<T : BlockChainData>(
        val data: T
    ) : DataResult<T>()

    data class QueryFailure<T : BlockChainData>(
        override val cause: String,
        val exception: Exception? = null
    ) : Failable, DataResult<T>()

    data class NonExistentData<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataResult<T>()

    data class UnrecognizedDataType<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataResult<T>()

    data class UnrecognizedUnit<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataResult<T>()

    data class UnexpectedClass<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataResult<T>()

    data class NonRegisteredSchema<T : BlockChainData>(
        override val cause: String
    ) : Failable, DataResult<T>()

    data class Propagated<T : BlockChainData>(
        val pointOfFailure: String,
        val failable: Failable
    ) : Failable, DataResult<T>() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }
}