package org.knowledger.ledger.core.results

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