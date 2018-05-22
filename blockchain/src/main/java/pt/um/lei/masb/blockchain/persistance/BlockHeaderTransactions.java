package pt.um.lei.masb.blockchain.persistance;

import pt.um.lei.masb.blockchain.BlockHeader;

import javax.persistence.EntityManager;
import java.util.Optional;

public final class BlockHeaderTransactions
        extends AbstractTransactionsWrapper<BlockHeader>
        implements TransactionsWrapper {
    public BlockHeaderTransactions(PersistenceWrapper p) {
        super(p);
    }

    public BlockHeaderTransactions() {
    }

    public Optional<BlockHeader> getBlockHeaderByHash(String hash) {
        return p.executeInSessionAndReturn(this::blockHeaderById, hash);
    }

    public Optional<BlockHeader> getBlockHeaderByBlockHeight(long height) {
        return p.executeInSessionAndReturn(this::blockHeaderByHeight, height);
    }


    private Optional<BlockHeader> blockHeaderById(EntityManager entityManager, String hash) {
        return findEntity(BlockHeader.class,
                          entityManager,
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
