package org.knowledger.ledger.service.transactions

import org.knowledger.ledger.database.query.UnspecificQuery
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import java.security.PublicKey

internal fun QueryManager.getWitnessInfoBy(
    publicKey: PublicKey
): Outcome<WitnessInfo, LoadFailure> =
    queryUniqueResult(
        UnspecificQuery(
            """
            SELECT hash, witness.index as index, max(blockheight) as max
            FROM 
                 (SELECT hash, blockheight, witnesses:{index, publicKey} as witness
                 FROM ${coinbaseStorageAdapter.id}
                 UNWIND witness)
            WHERE witness.publicKey = :publicKey
            """.trimIndent(),
            mapOf("publicKey" to publicKey.encoded)
        ), WitnessInfo
    )
