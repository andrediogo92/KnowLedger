package org.knowledger.ledger.results

private class HardFailureException(
    cause: String, exception: Exception
) : Exception(cause, exception) {
    override fun fillInStackTrace(): Throwable = this
}

private class FailureException(cause: String) : Exception(cause) {
    override fun fillInStackTrace(): Throwable = this
}

private fun Failable.HardFailure.hardUnwrap(): Exception =
    if (exception != null) {
        HardFailureException(cause, exception)
    } else {
        lightUnwrap()
    }

private fun Failable.lightUnwrap(): Exception =
    FailureException(cause)

private fun Failable.unwrap(): Exception =
    when (this) {
        is Failable.HardFailure -> hardUnwrap()
        is Failable.PropagatedFailure -> propagateUnwrap()
        is Failable.LightFailure -> lightUnwrap()
    }

private fun Failable.PropagatedFailure.propagateUnwrap(): Exception =
    when (inner) {
        is Failable.HardFailure -> {
            if (inner.exception != null) {
                HardFailureException(cause, inner.exception)
            } else {
                lightUnwrap()
            }
        }
        is Failable.PropagatedFailure -> {
            val extracted = extractException()
            if (extracted != null) {
                HardFailureException(cause, extracted)
            } else {
                lightUnwrap()
            }
        }
        is Failable.LightFailure -> lightUnwrap()
    }

private tailrec fun Failable.PropagatedFailure.extractException(): Exception? =
    when (inner) {
        is Failable.HardFailure -> inner.exception
        is Failable.PropagatedFailure -> inner.extractException()
        else -> null
    }

fun Failure.unwrap(): Nothing =
    throw failable.unwrap()