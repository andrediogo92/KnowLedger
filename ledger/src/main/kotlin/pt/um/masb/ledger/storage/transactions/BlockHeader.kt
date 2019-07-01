package pt.um.masb.ledger.storage.transactions

import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericSelect
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.adapters.BlockHeaderStorageAdapter


// ------------------------------
// Blockheader transactions.
//
// ------------------------------


internal fun PersistenceWrapper.getBlockHeaderByHash(
    ledgerHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "hashId",
                "hashId",
                hash
            ),
            it
        )
    }


internal fun PersistenceWrapper.getBlockHeaderByBlockHeight(
    ledgerHash: Hash,
    height: Long
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE, "blockheight",
                "blockheight",
                height
            ),
            it
        )

    }


internal fun PersistenceWrapper.getBlockHeaderByPrevHeaderHash(
    ledgerHash: Hash,
    hash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "previousHash",
                "hashId",
                hash
            ),
            it
        )

    }

internal fun PersistenceWrapper.getLatestBlockHeader(
    ledgerHash: Hash
): Outcome<BlockHeader, LoadFailure> =
    BlockHeaderStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id,
                "max(blockheight), *"
            ),
            it
        )
    }