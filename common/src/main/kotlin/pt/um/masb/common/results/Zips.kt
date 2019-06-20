package pt.um.masb.common.results

inline fun <T1, T2, R, U : Failable> zip(
    r1: Outcome<T1, U>,
    r2: Outcome<T2, U>,
    transform: (T1, T2) -> R
): Outcome<R, U> =
    r1.flatMapSuccess { v1 ->
        r2.mapSuccess { v2 ->
            transform(v1, v2)
        }
    }

inline fun <T1, T2, T3, R, U : Failable> zip(
    r1: Outcome<T1, U>,
    r2: Outcome<T2, U>,
    r3: Outcome<T3, U>,
    transform: (T1, T2, T3) -> R
): Outcome<R, U> =
    r1.flatMapSuccess { v1 ->
        r2.flatMapSuccess { v2 ->
            r3.mapSuccess { v3 ->
                transform(v1, v2, v3)
            }
        }
    }

inline fun <T1, T2, T3, T4, R, U : Failable> zip(
    r1: Outcome<T1, U>,
    r2: Outcome<T2, U>,
    r3: Outcome<T3, U>,
    r4: Outcome<T4, U>,
    transform: (T1, T2, T3, T4) -> R
): Outcome<R, U> =
    r1.flatMapSuccess { v1 ->
        r2.flatMapSuccess { v2 ->
            r3.flatMapSuccess { v3 ->
                r4.mapSuccess { v4 ->
                    transform(v1, v2, v3, v4)
                }
            }
        }
    }