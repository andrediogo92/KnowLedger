package org.knowledger.ledger.crypto.hash


class NoSuchHasherRegistered() : Exception() {
    override var message: String? = super.message

    constructor(
        digestLength: Int,
        algorithm: String,
        providerName: String,
        providerVersion: Double
    ) : this() {
        message = """No hasher with: 
            |   Digest length -> $digestLength
            |   Algorithm -> $algorithm
            |   Provider -> $providerName
            |   Provider Version -> $providerVersion
            """.trimMargin()
    }

    constructor(
        hash: Hash
    ) : this() {
        message = "No hasher with hash: $hash"
    }
}