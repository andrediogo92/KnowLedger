package org.knowledger.ledger.chain.transactions

import org.knowledger.ledger.chain.data.WitnessInfo
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.results.LoadFailure

internal fun QueryManager.getWitnessInfoBy(
    publicKey: EncodedPublicKey,
): Outcome<WitnessInfo, LoadFailure> {
    val query = UnspecificQuery(
        """ SELECT hash, witness.index as index, blockheight
            |FROM ${'$'}temp
            |LET ${'$'}temp =
                |(SELECT coinbaseHeader.hash as hash, 
                        |coinbaseHeader.blockheight as blockheight, 
                        |witnesses:{index, publicKey} as witness
                |FROM ${coinbaseStorageAdapter.id}
                |UNWIND witness),
            |${'$'}blockheight = (SELECT max(blockheight) FROM ${'$'}temp)
            WHERE witness.publicKey = :publicKey AND blockheight = ${'$'}blockheight
        """.trimMargin(), mapOf("publicKey" to publicKey.bytes)
    )
    return queryUniqueResult(query, witnessInfoServiceLoadable)
}
