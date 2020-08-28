package org.knowledger.ledger.crypto.hash

import org.knowledger.ledger.crypto.Hash


class NoSuchHasherRegistered() : Exception() {
    override var message: String? = super.message

    constructor(digestLength: Int, algorithm: String) : this() {
        message = """No hasher with: 
            |   Digest length -> $digestLength
            |   Algorithm -> $algorithm
            """.trimMargin()
    }

    constructor(hash: Hash) : this() {
        message = "No hasher with hash: $hash"
    }
}