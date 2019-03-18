package pt.um.lei.masb.blockchain.persistance.results

import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.utils.Failable

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
}