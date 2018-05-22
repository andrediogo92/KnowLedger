package pt.um.lei.masb.blockchain.persistance;

import pt.um.lei.masb.blockchain.Transaction;

import javax.persistence.EntityManager;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

public final class TransactionTransactions
        extends AbstractTransactionsWrapper<Transaction>
        implements TransactionsWrapper {
    public TransactionTransactions(PersistenceWrapper p) {
        super(p);
    }

    public TransactionTransactions() {
    }

    public List<Transaction> getTransactionsFromAgent(PublicKey publicKey) {
        return p.executeInSessionAndReturn(this::transactionByPubKey, publicKey);
    }

    public Optional<Transaction> getTransactionByHash(String hash) {
        return p.executeInSessionAndReturn(this::transactionById, hash);
    }

    public List<Transaction> getTransactionsOrderedByTimestamp() {
        return p.executeInSessionAndReturn(this::transactionsOrderedByTimestampId);
    }

    private List<Transaction> transactionsOrderedByTimestampId(EntityManager entityManager) {
        return getResults(Transaction.class,
                          entityManager,
                          "transactions_from_agent",
                          LOGGER);
    }

    private Optional<Transaction> transactionById(EntityManager entityManager,
                                                  String hash) {
        return findEntity(Transaction.class,
                          entityManager,
                          hash,
                          LOGGER);
    }

    private List<Transaction> transactionByPubKey(EntityManager entityManager,
                                                  PublicKey publicKey) {
        return getResultsSingleParameter(Transaction.class,
                                         entityManager,
                                         "transactions_from_agent",
                                         "publicKey",
                                         publicKey,
                                         LOGGER);
    }


}
