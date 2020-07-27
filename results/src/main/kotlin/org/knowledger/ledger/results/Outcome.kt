package org.knowledger.ledger.results

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

typealias Outcome<T, U> = Result<T, U>

fun <T : Any> T.ok(): Ok<T> = Ok(this)
fun <T : Any> T.err(): Err<T> = Err(this)