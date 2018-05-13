package pt.um.lei.masb.blockchain.persistance;


import pt.um.lei.masb.blockchain.Ident;

import javax.persistence.EntityManager;
import java.util.Optional;

public final class IdentTransactions
        extends AbstractTransactionsWrapper
        implements TransactionsWrapper {

    public Optional<Ident> getIdent() {
        return p.executeInSessionAndReturn(this::UniqueIdent);
    }

    private Optional<Ident> UniqueIdent(EntityManager entityManager) {
        return getUniqueResult(Ident.class,
                               entityManager,
                               "get_ident",
                               LOGGER);
    }

}
