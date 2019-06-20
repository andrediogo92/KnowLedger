package pt.um.masb.common.results

sealed class Outcome<out T, out U : Failable> {
    data class Ok<out T>(
        val value: T
    ) : Outcome<T, Nothing>() {
        inline fun <M, S : Failable> flatMap(
            apply: (T) -> Outcome<M, S>
        ): Outcome<M, S> =
            apply(this.value)

        inline fun <R> intoSequence(
            reduce: (T) -> Sequence<R>
        ): Ok<Sequence<R>> =
            Ok(reduce(this.value))

        inline fun <M> map(apply: (T) -> M): Ok<M> =
            Ok(apply(value))

    }

    data class Error<out U : Failable>(
        val failure: U
    ) : Outcome<Nothing, U>() {

        inline fun <M, S : Failable> flatMap(
            apply: (U) -> Outcome<M, S>
        ): Outcome<M, S> =
            apply(this.failure)

        inline fun <S : Failable> map(
            apply: (U) -> S
        ): Error<S> =
            Error(apply(failure))
    }
}

/**
 * Call [block] and wrap the result in an [Outcome], catching any
 * [Exception] and transforming through [error] into a [Failable] value.
 */
inline fun <T, U : Failable> resultFrom(
    block: () -> T,
    error: (Exception) -> U
): Outcome<T, U> =
    try {
        Outcome.Ok(block())
    } catch (x: Exception) {
        Outcome.Error(error(x))
    }


inline fun <T, M, U : Failable, S : Failable> Outcome<T, U>.fold(
    apply: (Outcome<T, U>) -> Outcome<M, S>
): Outcome<M, S> = apply(this)

inline fun <T, M, U : Failable, S : Failable> Outcome<T, U>.fold(
    onError: (U) -> Outcome<M, S>,
    onSuccess: (T) -> Outcome<M, S>
): Outcome<M, S> =
    when (this) {
        is Outcome.Ok -> this.flatMap(onSuccess)
        is Outcome.Error -> this.flatMap(onError)
    }

inline fun <T, M, U : Failable> Outcome<T, U>.flatMapSuccess(
    onSuccess: (T) -> Outcome<M, U>
): Outcome<M, U> =
    when (this) {
        is Outcome.Ok -> this.flatMap(onSuccess)
        is Outcome.Error -> this
    }

inline fun <T, U : Failable, V : Failable> Outcome<T, U>.flatMapFailure(
    onError: (U) -> Outcome<T, V>
): Outcome<T, V> =
    when (this) {
        is Outcome.Ok -> Outcome.Ok(this.value)
        is Outcome.Error -> this.flatMap(onError)
    }

inline fun <T, M, U : Failable> Outcome<T, U>.mapSuccess(
    onSuccess: (T) -> M
): Outcome<M, U> =
    when (this) {
        is Outcome.Ok -> this.map(onSuccess)
        is Outcome.Error -> this
    }

inline fun <T, U : Failable, S : Failable> Outcome<T, U>.mapFailure(
    onError: (U) -> S
): Outcome<T, S> =
    when (this) {
        is Outcome.Ok -> this
        is Outcome.Error -> this.map(onError)
    }

/**
 * Perform computation purely for side effects over the [Outcome].
 */
inline fun <T, U : Failable> Outcome<T, U>.reduce(
    onSuccess: (T) -> Unit,
    onError: (U) -> Unit
) {
    when (this) {
        is Outcome.Ok -> onSuccess(this.value)
        is Outcome.Error -> onError(this.failure)
    }
}

/**
 * Perform reduction to [M] for both possible [Outcome] cases.
 */
inline fun <M, T, U : Failable> Outcome<T, U>.reduce(
    onSuccess: (T) -> M,
    onError: (U) -> M
): M =
    when (this) {
        is Outcome.Ok -> onSuccess(this.value)
        is Outcome.Error -> onError(this.failure)
    }

/**
 * Perform computation purely for side effects with the success value.
 *
 * Will only execute if [Outcome] is [Outcome.Ok].
 */
inline fun <T, U : Failable> Outcome<T, U>.peekSuccess(onSuccess: (T) -> Unit): Outcome<T, U> =
    apply { if (this is Outcome.Ok<T>) onSuccess(value) }

/**
 * Perform computation purely for side effects with the failure value.
 *
 * Will only execute if [Outcome] is [Outcome.Error].
 */
inline fun <T, U : Failable> Outcome<T, U>.peekFailure(onError: (U) -> Unit): Outcome<T, U> =
    apply { if (this is Outcome.Error<U>) onError(failure) }

/**
 * Unwraps the [Outcome] if it is [Outcome.Ok].
 * @throws RuntimeException When there is an [Outcome.Error] present.
 */
fun <T, U : Failable> Outcome<T, U>.unwrap(): T =
    when (this) {
        is Outcome.Ok -> this.value
        is Outcome.Error -> throw RuntimeException(this.failure.cause)
    }

/**
 * Unwrap the [Outcome] by returning the success value or calling
 * [errorToValue] mapping the failure reason to a plain value.
 */
inline fun <M, T : M, R : M, U : Failable> Outcome<T, U>.recover(
    errorToValue: U.() -> R
): M =
    when (this) {
        is Outcome.Ok -> this.value
        is Outcome.Error -> this.failure.errorToValue()
    }

/**
 * Unwrap the [Outcome], by returning the success value or calling
 * [block] on failure to abort from the current function.
 */
@Suppress("IMPLICIT_NOTHING_AS_TYPE_PARAMETER")
inline fun <T, U : Failable> Outcome<T, U>.onFailure(
    block: (Outcome.Error<U>) -> Nothing
): T =
    when (this) {
        is Outcome.Ok<T> -> value
        is Outcome.Error<U> -> block(this)
    }