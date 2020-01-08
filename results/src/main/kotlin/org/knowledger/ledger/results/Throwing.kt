package org.knowledger.ledger.results

private fun Failable.HardFailure.hardUnwrap(): Exception =
    if (exception != null) {
        RuntimeException(cause, exception)
    } else {
        unwrap()
    }

private fun Failable.unwrap(): Exception =
    when (this) {
        is Failable.HardFailure -> hardUnwrap()
        is Failable.PropagatedFailure -> propagateUnwrap()
        is Failable.LightFailure -> throw RuntimeException(cause)
    }

private fun Failable.PropagatedFailure.propagateUnwrap(): Exception =
    when (inner) {
        is Failable.HardFailure -> {
            val hardFailure = inner
            if (hardFailure.exception != null) {
                RuntimeException(cause, hardFailure.exception)
            } else {
                RuntimeException(cause)
            }
        }
        is Failable.PropagatedFailure -> {
            val extracted = extractException()
            if (extracted != null) {
                RuntimeException(cause, extracted)
            } else {
                RuntimeException(cause)
            }
        }
        is Failable.LightFailure -> RuntimeException(cause)
    }

private fun Failable.PropagatedFailure.extractException(): Exception? =
    when (inner) {
        is Failable.HardFailure -> inner.exception
        else -> null
    }

fun Failure.unwrap(): Nothing =
    throw failable.unwrap()