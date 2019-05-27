package pt.um.masb.common.results

sealed class Outcome<T : Any, U : Failable> {
    data class Ok<T : Any, U : Failable>(
        val data: T
    ) : Outcome<T, U>() {
        inline fun <M : Any, S : Failable> mapResult(
            apply: Ok<T, U>.() -> Outcome<M, S>
        ): Outcome<M, S> =
            this.apply()

        inline fun <R : Any> intoSequence(
            reduce: T.() -> Sequence<R>
        ): Ok<Sequence<R>, U> =
            Ok(this.data.reduce())

        inline fun <M : Any> map(apply: T.() -> M): Ok<M, U> =
            Ok(data.apply())

        override fun unwrap(): T =
            data

    }

    data class Error<T : Any, U : Failable>(
        val failure: U
    ) : Outcome<T, U>() {
        override fun unwrap(): T =
            throw RuntimeException(failure.cause)

        inline fun <M : Any, S : Failable> mapResult(
            apply: Error<T, U>.() -> Outcome<M, S>
        ): Outcome<M, S> =
            this.apply()
    }

    inline fun <M : Any, S : Failable> mapToNew(
        apply: Outcome<T, U>.() -> Outcome<M, S>
    ): Outcome<M, S> = this.apply()

    inline fun <M : Any, S : Failable> mapToNew(
        onError: Error<T, U>.() -> Outcome<M, S>,
        onSuccess: Ok<T, U>.() -> Outcome<M, S>
    ): Outcome<M, S> =
        when (this) {
            is Ok -> this.mapResult(onSuccess)
            is Error -> this.mapResult(onError)
        }

    inline fun <M : Any> mapSuccess(
        onSuccess: Ok<T, U>.() -> Outcome<M, U>
    ): Outcome<M, U> =
        when (this) {
            is Ok -> this.mapResult(onSuccess)
            is Error -> Error(this.failure)
        }

    inline fun <V : Failable> mapError(
        onError: Error<T, U>.() -> Outcome<T, V>
    ): Outcome<T, V> =
        when (this) {
            is Ok -> Ok(this.data)
            is Error -> this.mapResult(onError)
        }

    inline fun <M : Any> flatMapSuccess(
        apply: T.() -> M
    ): Outcome<M, U> =
        when (this) {
            is Ok -> this.map(apply)
            is Error -> Error(this.failure)
        }


    abstract fun unwrap(): T
}