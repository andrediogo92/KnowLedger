package pt.um.masb.common.results

/**
 * Attempts to extract all values or short circuits on first failure.
 */
fun <T, U : Failable> Iterable<Outcome<T, U>>.allValues(): Outcome<List<T>, U> =
    Outcome.Ok(
        map { r -> r.onFailure { return it } }
    )

/**
 * Attempts to extract all values or short circuits on first failure.
 */
fun <T, U : Failable> List<Outcome<T, U>>.allValues(): Outcome<List<T>, U> =
    Outcome.Ok(
        map { r -> r.onFailure { return it } }
    )

/**
 * Attempts to extract all values or short circuits on first failure.
 */
fun <T, U : Failable> Sequence<Outcome<T, U>>.allValues(): Outcome<Sequence<T>, U> {
    val accumulator: MutableList<T> = mutableListOf()
    var short = false
    lateinit var shorter: U
    loop@ for (shorting in this) {
        when (shorting) {
            is Outcome.Ok -> accumulator += shorting.value
            is Outcome.Error -> {
                shorter = shorting.failure
                short = true
                break@loop
            }
        }
    }
    return if (short) {
        Outcome.Error(shorter)
    } else {
        Outcome.Ok(
            accumulator.asSequence()
        )
    }
}

/**
 * Extracts all successful [Outcome]s, discards failures.
 */
fun <T, U : Failable> Iterable<Outcome<T, U>>.anyValues(): List<T> =
    filterIsInstance<Outcome.Ok<T>>().map {
        it.value
    }

/**
 * Extracts all successful [Outcome]s, discards failures.
 */
fun <T, U : Failable> List<Outcome<T, U>>.anyValues(): List<T> =
    filterIsInstance<Outcome.Ok<T>>().map {
        it.value
    }

/**
 * Extracts all successful [Outcome]s, discards failures.
 */
fun <T, U : Failable> Sequence<Outcome<T, U>>.anyValues(): Sequence<T> =
    filterIsInstance<Outcome.Ok<T>>().map {
        it.value
    }

/**
 * Extracts all successful [Outcome]s, and all failed [Outcome]s into a pair.
 */
fun <T, U : Failable> Iterable<Outcome<T, U>>.partition(): Pair<List<T>, List<U>> {
    val oks = mutableListOf<T>()
    val errs = mutableListOf<U>()
    forEach {
        when (it) {
            is Outcome.Ok<T> -> oks.add(it.value)
            is Outcome.Error<U> -> errs.add(it.failure)
        }
    }
    return Pair(oks, errs)
}

/**
 * Extracts all successful [Outcome]s, and all failed [Outcome]s into a pair.
 */
fun <T, U : Failable> List<Outcome<T, U>>.partition(): Pair<List<T>, List<U>> {
    val oks = mutableListOf<T>()
    val errs = mutableListOf<U>()
    forEach {
        when (it) {
            is Outcome.Ok<T> -> oks.add(it.value)
            is Outcome.Error<U> -> errs.add(it.failure)
        }
    }
    return Pair(oks, errs)
}


/**
 * Extracts all successful [Outcome]s, and all failed [Outcome]s into a pair.
 */
fun <T, U : Failable> Sequence<Outcome<T, U>>.partition(): Pair<List<T>, List<U>> {
    val oks = mutableListOf<T>()
    val errs = mutableListOf<U>()
    forEach {
        when (it) {
            is Outcome.Ok<T> -> oks.add(it.value)
            is Outcome.Error<U> -> errs.add(it.failure)
        }
    }
    return Pair(oks, errs)
}