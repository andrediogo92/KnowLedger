package pt.um.lei.masb.blockchain.persistance;

import pt.um.lei.masb.blockchain.Block;

import javax.persistence.EntityManager;
import java.util.Optional;

public final class BlockTransactions
        extends AbstractTransactionsWrapper<Block>
        implements TransactionsWrapper {

    public Optional<Block> getBlockByBlockHeight(long blockheight) {
        return p.executeInSessionAndReturn(this::blockById, blockheight);
    }

    public Optional<Block> getBlockByHeaderHash(String hash) {
        return p.executeInSessionAndReturn(this::blockByHash, hash);
    }

    public Optional<Block> getLatestBlock() {
        return p.executeInSessionAndReturn(this::latestBlock);
    }

    public Optional<Block> getBlockByPrevHeaderHash(String hash) {
        return p.executeInSessionAndReturn(this::prevBlock, hash);
    }

    private Optional<Block> blockById(EntityManager entityManager, long blockheight) {
        return getUniqueResultSingleParameter(Block.class,
                                              entityManager,
                                              "get_block_by_height",
                                              "height",
                                              blockheight,
                                              LOGGER);
    }

    private Optional<Block> blockByHash(EntityManager entityManager, String hash) {
        return getUniqueResultSingleParameter(Block.class,
                                              entityManager,
                                              "get_block_by_hash",
                                              "hash",
                                              hash,
                                              LOGGER);
    }


    private Optional<Block> latestBlock(EntityManager entityManager) {
        return getUniqueResult(Block.class,
                               entityManager,
                               "get_latest_block",
                               LOGGER);
    }


    private Optional<Block> prevBlock(EntityManager entityManager, String hash) {
        return getUniqueResultSingleParameter(Block.class,
                                              entityManager,
                                              "get_prev_block_by_hash",
                                              "hash",
                                              hash,
                                              LOGGER);
    }
}
