package pt.um.lei.masb.blockchain.ledger

import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.persistance.loaders.ChainLoadable
import pt.um.lei.masb.blockchain.persistance.loaders.DefaultLoadable
import pt.um.lei.masb.blockchain.service.CategoryTypes
import pt.um.lei.masb.blockchain.service.ChainHandle
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.service.LedgerHandle


/**
 * [LedgerContract] applies to top-level
 * blockchain classes.
 *
 * This interface serves as a bound for
 * [DefaultLoadable] and [ChainLoadable].
 *
 * These are:
 * - [Block];
 * - [LedgerHandle];
 * - [LedgerId];
 * - [LedgerParams];
 * - [BlockHeader];
 * - [CategoryTypes];
 * - [Coinbase];
 * - [Ident];
 * - [PhysicalData];
 * - [MerkleTree];
 * - [ChainHandle];
 * - [Transaction];
 * - [TransactionOutput].
 */
interface LedgerContract