package pt.um.lei.masb.blockchain.persistance;

import javax.persistence.EntityManager;
import java.util.logging.Logger;

abstract class AbstractTransactionsWrapper<T> implements TransactionsWrapper {
    protected static Logger LOGGER = Logger.getLogger("TransactionsWrapper");
    protected PersistenceWrapper p;

    AbstractTransactionsWrapper(PersistenceWrapper p) {
        this.p = p;
    }

    AbstractTransactionsWrapper() {
        p = PersistenceWrapper.getInstance();
    }


    /**
     * Should be called when querying is finished to reset the persistence context.
     */
    public void clearTransactionsContext() {
        p.closeCurrentSession();
    }

    public boolean persistEntity(T entity) {
        return p.executeInCurrentSession(this::persist, entity);
    }

    public boolean updateEntity(T entity) {
        return p.executeInCurrentSession(this::update, entity);
    }

    private boolean persist(EntityManager entityManager, T r) {
        return persistEntity(entityManager, r, LOGGER);
    }


    private boolean update(EntityManager entityManager, T entity) {
        return mergeEntity(entityManager, entity, LOGGER);
    }


}
