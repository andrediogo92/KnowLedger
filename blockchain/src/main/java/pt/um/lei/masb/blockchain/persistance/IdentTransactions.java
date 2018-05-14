package pt.um.lei.masb.blockchain.persistance;


import pt.um.lei.masb.blockchain.Ident;

import javax.persistence.EntityManager;
import java.util.Optional;

public final class IdentTransactions
        extends AbstractTransactionsWrapper<Ident>
        implements TransactionsWrapper {

    public Optional<Ident> getIdent() {
        return p.executeInSessionAndReturn(this::UniqueIdent);
    }

    private Optional<Ident> UniqueIdent(EntityManager entityManager) {
        return findEntity(Ident.class,
                          entityManager,
                          0,
                          LOGGER);
    }

}
