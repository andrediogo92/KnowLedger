package org.knowledger.ledger.results

/**
 * Attempts to extract all values or short circuits on first failure.
 */
fun <T, U : Failure> Iterable<Outcome<T, U>>.allValues(): Outcome<List<T>, U> =
    mutableListOf<T>().let { accumulator ->
        extractOrShort(accumulator)?.let {
            Outcome.Error(it)
        } ?: Outcome.Ok(accumulator)
    }

/**
 * Attempts to extract all values or short circuits on first failure.
 */
fun <T, U : Failure> Collection<Outcome<T, U>>.allValues(): Outcome<List<T>, U> =
    ArrayList<T>(size).let { accumulator ->
        asIterable().extractOrShort(accumulator)?.let {
            Outcome.Error(it)
        } ?: Outcome.Ok(accumulator)
    }

/**
 * Attempts to extract all values or short circuits on first failure.
 */
fun <T, U : Failure> Sequence<Outcome<T, U>>.allValues(): Outcome<Sequence<T>, U> =
    mutableListOf<T>().let { accumulator ->
        asIterable().extractOrShort(accumulator)?.let {
            Outcome.Error(it)
        } ?: Outcome.Ok(accumulator.asSequence())
    }

private fun <T, U : Failure> Iterable<Outcome<T, U>>.extractOrShort(
    accumulator: MutableList<T>
): U? {
    var shorter: U? = null
    loop@ for (shorting in this) {
        when (shorting) {
            is Outcome.Ok -> accumulator += shorting.value
            is Outcome.Error -> {
                shorter = shorting.failure
                break@loop
            }
        }
    }
    return shorter
}

/**
 * Extracts all successful [Outcome]s, discards failures.
 */
fun <T, U : Failure> Iterable<Outcome<T, U>>.anyValues(): List<T> =
    filterIsInstance<Outcome.Ok<T>>().map {
        it.value
    }

/**
 * Extracts all successful [Outcome]s, discards failures.
 */
fun <T, U : Failure> Sequence<Outcome<T, U>>.anyValues(): Sequence<T> =
    filterIsInstance<Outcome.Ok<T>>().map {
        it.value
    }

/**
 * Extracts all successful [Outcome]s, and all failed [Outcome]s into a pair.
 */
@Suppress("DuplicatedCode")
fun <T, U : Failure> Iterable<Outcome<T, U>>.partition(): Pair<List<T>, List<U>> =
    extractByTwo(mutableListOf(), mutableListOf())


/**
 * Extracts all successful [Outcome]s, and all failed [Outcome]s into a pair.
 */
fun <T, U : Failure> Sequence<Outcome<T, U>>.partition(): Pair<List<T>, List<U>> =
    asIterable().partition()

/**
 * Extracts all successful [Outcome]s, and all failed [Outcome]s into a pair.
 */
fun <T, U : Failure> Collection<Outcome<T, U>>.partition(): Pair<List<T>, List<U>> =
    extractByTwo(ArrayList(size / 2), ArrayList(size / 2))

private fun <T, U : Failure> Iterable<Outcome<T, U>>.extractByTwo(
    oks: MutableList<T>, errs: MutableList<U>
): Pair<List<T>, List<U>> {
    forEach {
        when (it) {
            is Outcome.Ok<T> -> oks.add(it.value)
            is Outcome.Error<U> -> errs.add(it.failure)
        }
    }
    return Pair(oks, errs)
}