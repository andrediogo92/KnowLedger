package org.knowledger.ledger.core.base.hash

import org.knowledger.ledger.core.base.data.Tag

fun <T : Any> T.classDigest(hasher: Hasher): Tag =
    hasher.applyHash(
        this::class.java.toGenericString()
    )

fun <T> Class<T>.classDigest(hasher: Hasher): Tag =
    hasher.applyHash(
        toGenericString()
    )