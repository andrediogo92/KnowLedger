package pt.um.lei.masb.blockchain.persistance;

import pt.um.lei.masb.blockchain.BlockHeader;

import javax.persistence.EntityManager;
import java.util.Optional;

public final class BlockHeaderTransactions
        extends AbstractTransactionsWrapper
        implements TransactionsWrapper {

    public Optional<BlockHeader> getBlockHeaderByHash(String hash) {
        return p.executeInSessionAndReturn(this::blockHeaderById, hash);
    }

    public Optional<BlockHeader> getBlockHeaderByBlockHeight(long height) {
        return p.executeInSessionAndReturn(this::blockHeaderByHeight, height);
    }


    private Optional<BlockHeader> blockHeaderById(EntityManager entityManager, String hash) {
        return getUniqueResultSingleParameter(BlockHeader.class,
                                              entityManager,
                                              "blockheader_by_hash",
                                              "hash",
                                              hash,
                                              LOGGER);
    }

    private Optional<BlockHeader> blockHeaderByHeight(EntityManager entityManager,
                                                      long height) {
        return getUniqueResultSingleParameter(BlockHeader.class,
                                              entityManager,
                                              "blockheader_by_height",
                                              "height",
                                              height,
                                              LOGGER);
    }


}
