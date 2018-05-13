package pt.um.lei.masb.blockchain.persistance;

import java.util.logging.Logger;

abstract class AbstractTransactionsWrapper implements TransactionsWrapper {
    protected static Logger LOGGER = Logger.getLogger("TransactionsWrapper");
    protected PersistanceWrapper p = PersistanceWrapper.getInstance();

    /**
     * Should be called when querying is finished to reset the persistence context.
     */
    public void clearTransactionsContext() {
        p.closeCurrentSession();
    }
}
