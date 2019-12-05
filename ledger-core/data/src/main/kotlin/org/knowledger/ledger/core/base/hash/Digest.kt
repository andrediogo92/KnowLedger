package org.knowledger.ledger.core.base.hash

import org.knowledger.ledger.core.base.data.Tag

fun <T : Any> T.classDigest(hasher: Hasher): Tag =
    this.javaClass.classDigest(hasher)

fun <T> Class<T>.classDigest(hasher: Hasher): Tag =
    hasher.applyHash(
        toGenericString()
    )