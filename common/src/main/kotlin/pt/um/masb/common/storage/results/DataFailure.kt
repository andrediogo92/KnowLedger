package pt.um.masb.common.storage.results

import pt.um.masb.common.results.Failable


sealed class DataFailure : Failable {
    data class NonExistentData(
        override val cause: String
    ) : DataFailure()

    data class UnrecognizedDataType(
        override val cause: String
    ) : DataFailure()

    data class UnrecognizedUnit(
        override val cause: String
    ) : DataFailure()

    data class UnexpectedClass(
        override val cause: String
    ) : DataFailure()

    data class NonRegisteredSchema(
        override val cause: String
    ) : DataFailure()

    /**
     * Reserved for direct irrecoverable errors.
     * Query failures will wrap exceptions if thrown.
     */
    data class UnknownFailure(
        override val cause: String,
        val exception: Exception? = null
    ) : DataFailure()

    /**
     * Reserved for indirect irrecoverable errors propagated
     * by some internal result.
     */
    data class Propagated(
        val pointOfFailure: String,
        val failable: Failable
    ) : DataFailure() {
        override val cause: String
            get() = "$pointOfFailure: ${failable.cause}"
    }
}