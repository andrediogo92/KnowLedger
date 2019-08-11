package org.knowledger.ledger.service

import org.knowledger.ledger.builders.Builder
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.BuilderFailure
import org.knowledger.ledger.service.handles.LedgerHandle

/**
 *
 */
data class LedgerConfig(
    val ledgerId: LedgerId,
    val ledgerParams: LedgerParams,
    val coinbaseParams: CoinbaseParams
) {
    data class ByConfigBuilder @PublishedApi internal constructor(
        internal val ledgerConfig: LedgerConfig,
        internal val chainId: ChainId
    ) : Builder<ByConfigBuilder, BuilderFailure> {
        internal lateinit var ledgerContainer: LedgerContainer

        override fun build(): Outcome<ByConfigBuilder, BuilderFailure> {
            val container = LedgerHandle.getContainer(
                ledgerConfig.ledgerId.hashId
            )
            return when (container) {
                null ->
                    Outcome.Error(
                        BuilderFailure.ParameterNotRegistered(
                            "There is no active LedgerHandle#${ledgerConfig.ledgerId.tag} with corresponding hash: ${ledgerConfig.ledgerId.hashId} to derive builder."
                        )
                    )
                else -> {
                    ledgerContainer = container
                    Outcome.Ok(this)
                }
            }
        }
    }
}

