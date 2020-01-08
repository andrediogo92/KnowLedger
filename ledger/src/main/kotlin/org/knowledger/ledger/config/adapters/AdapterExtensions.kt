package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LedgerFailure

internal fun BlockParams.persist(
    session: ManagedSession
): StorageElement =
    BlockParamsStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadBlockParams(
    ledgerHash: Hash
): Outcome<BlockParams, LedgerFailure> =
    BlockParamsStorageAdapter.load(
        ledgerHash, this
    )

internal fun ChainId.persist(
    session: ManagedSession
): StorageElement =
    ChainIdStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadChainId(
    ledgerHash: Hash
): Outcome<ChainId, LedgerFailure> =
    ChainIdStorageAdapter.load(
        ledgerHash, this
    )

internal fun CoinbaseParams.persist(
    session: ManagedSession
): StorageElement =
    CoinbaseParamsStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadCoinbaseParams(
    ledgerHash: Hash
): Outcome<CoinbaseParams, LedgerFailure> =
    CoinbaseParamsStorageAdapter.load(
        ledgerHash, this
    )

internal fun LedgerId.persist(
    session: ManagedSession
): StorageElement =
    LedgerIdStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadLedgerId(
    ledgerHash: Hash
): Outcome<LedgerId, LedgerFailure> =
    LedgerIdStorageAdapter.load(
        ledgerHash, this
    )

internal fun LedgerParams.persist(
    session: ManagedSession
): StorageElement =
    LedgerParamsStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadLedgerParams(
    ledgerHash: Hash
): Outcome<LedgerParams, LedgerFailure> =
    LedgerParamsStorageAdapter.load(
        ledgerHash, this
    )