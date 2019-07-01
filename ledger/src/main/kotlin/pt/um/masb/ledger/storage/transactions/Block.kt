package pt.um.masb.ledger.storage.transactions

import pt.um.masb.common.database.query.Filters
import pt.um.masb.common.database.query.GenericSelect
import pt.um.masb.common.database.query.SimpleBinaryOperator
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.adapters.BlockStorageAdapter

// ------------------------------
// Block transactions.
//
// ------------------------------


internal fun PersistenceWrapper.getBlockByBlockHeight(
    ledgerHash: Hash,
    blockheight: Long
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "header.blockheight",
                "blockheight",
                blockheight
            ),
            it
        )

    }


internal fun PersistenceWrapper.getBlockByHeaderHash(
    ledgerHash: Hash,
    hash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "header.hashId",
                "hashId",
                hash
            ),
            it
        )

    }


internal fun PersistenceWrapper.getBlockByPrevHeaderHash(
    ledgerHash: Hash,
    hash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withSimpleFilter(
                Filters.WHERE,
                "header.previousHash",
                "hashId",
                hash
            ),
            it
        )

    }


internal fun PersistenceWrapper.getLatestBlock(
    ledgerHash: Hash
): Outcome<Block, LoadFailure> =
    BlockStorageAdapter.let {
        queryUniqueResult(
            ledgerHash,
            GenericSelect(
                it.id
            ).withProjection(
                "max(header.blockheight), *"
            ),
            it
        )
    }

internal fun PersistenceWrapper.getBlockListByBlockHeightInterval(
    ledgerHash: Hash,
    startInclusive: Long,
    endInclusive: Long
): Outcome<Sequence<Block>, LoadFailure> =
    BlockStorageAdapter.let {
        queryResults(
            ledgerHash,
            GenericSelect(
                it.id
            ).withBetweenFilter(
                Filters.WHERE,
                "header.blockheight",
                Pair("start", "end"),
                Pair(startInclusive, endInclusive),
                SimpleBinaryOperator.AND
            ),
            it
        )
    }

