package pt.um.masb.ledger

import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.service.CategoryTypes
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.Ident
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.storage.loaders.ChainLoadable
import pt.um.masb.ledger.storage.loaders.DefaultLoadable


/**
 * [LedgerContract] applies to top-level
 * ledger classes.
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