package pt.um.lei.masb.blockchain.persistance;

import pt.um.lei.masb.blockchain.BlockChain;

import javax.persistence.EntityManager;
import java.util.Optional;

public class BlockChainTransactions extends AbstractTransactionsWrapper<BlockChain> implements TransactionsWrapper {
    public Optional<BlockChain> getBlockChain() {
        return p.executeInSessionAndReturn(this::uniqueBlockChain);
    }

    private Optional<BlockChain> uniqueBlockChain(EntityManager entityManager) {
        return findEntity(BlockChain.class, entityManager, 0, LOGGER);
    }


}
