package org.knowledger.ledger.core.results

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

inline fun <T1, T2, T3, T4, T5, R, U : Failable> zip(
    r1: Outcome<T1, U>,
    r2: Outcome<T2, U>,
    r3: Outcome<T3, U>,
    r4: Outcome<T4, U>,
    r5: Outcome<T5, U>,
    transform: (T1, T2, T3, T4, T5) -> R
): Outcome<R, U> =
    r1.flatMapSuccess { v1 ->
        r2.flatMapSuccess { v2 ->
            r3.flatMapSuccess { v3 ->
                r4.flatMapSuccess { v4 ->
                    r5.mapSuccess { v5 ->
                        transform(v1, v2, v3, v4, v5)
                    }
                }
            }
        }
    }

inline fun <T1, T2, T3, T4, T5, T6, R, U : Failable> zip(
    r1: Outcome<T1, U>,
    r2: Outcome<T2, U>,
    r3: Outcome<T3, U>,
    r4: Outcome<T4, U>,
    r5: Outcome<T5, U>,
    r6: Outcome<T6, U>,
    transform: (T1, T2, T3, T4, T5, T6) -> R
): Outcome<R, U> =
    r1.flatMapSuccess { v1 ->
        r2.flatMapSuccess { v2 ->
            r3.flatMapSuccess { v3 ->
                r4.flatMapSuccess { v4 ->
                    r5.flatMapSuccess { v5 ->
                        r6.mapSuccess { v6 ->
                            transform(v1, v2, v3, v4, v5, v6)
                        }
                    }
                }
            }
        }
    }