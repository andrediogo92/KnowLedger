package org.knowledger.ledger.storage.results

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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


inline fun <T, R : Failure> tryOrConvertToFailure(
    function: () -> Outcome<T, R>,
    failureConstructor: (Exception) -> R
): Outcome<T, R> {
    contract {
        callsInPlace(function, InvocationKind.EXACTLY_ONCE)
        callsInPlace(failureConstructor, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        function()
    } catch (e: Exception) {
        failureConstructor(e).err()
    }
}

fun Failure.unwrap(): Nothing =
    throw failable.unwrap()

fun <T : Failure> Err<T>.unwrapFailure(): Nothing =
    error.unwrap()

fun <T : Any, U : Failure> Outcome<T, U>.unwrapFailure(): T =
    onFailure { it.unwrap() }.get()!!

inline fun <T : Failable, R : Failure> T.propagate(
    cons: (String, Failable) -> R
): R {
    contract {
        callsInPlace(cons, InvocationKind.EXACTLY_ONCE)
    }
    return cons(this.javaClass.simpleName, this)
}
