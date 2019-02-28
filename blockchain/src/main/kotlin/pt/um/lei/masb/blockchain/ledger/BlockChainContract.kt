package pt.um.lei.masb.blockchain.ledger

import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.data.PhysicalData

/**
 * [BlockChainContract] applies to top-level
 * blockchain classes.
 *
 * This interface serves as a bound for [DefaultLoadable].
 *
 * These are:
 * - [Block];
 * - [BlockChain];
 * - [BlockChainId];
 * - [BlockHeader];
 * - [CategoryTypes];
 * - [Coinbase];
 * - [Ident];
 * - [PhysicalData];
 * - [MerkleTree];
 * - [SideChain];
 * - [Transaction];
 * - [TransactionOutput].
 */
interface BlockChainContract