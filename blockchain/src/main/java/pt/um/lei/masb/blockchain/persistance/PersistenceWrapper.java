package pt.um.lei.masb.blockchain.persistance;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A Thread-safe wrapper into the JPA context of the blockchain.
 */
public final class PersistenceWrapper {
    private static final PersistenceWrapper PERSISTENCE_WRAPPER = new PersistenceWrapper();

    private final EntityManagerFactory sessionFactory;
    private EntityManager entityManager;

    private PersistenceWrapper() {
        sessionFactory = Persistence.createEntityManagerFactory("blockchainUnit");
    }

    public PersistenceWrapper(String persistenceUnit) {
        sessionFactory = Persistence.createEntityManagerFactory(persistenceUnit,
                                                                null);
    }

    static PersistenceWrapper getInstance() {
        return PERSISTENCE_WRAPPER;
    }

    synchronized <R> PersistenceWrapper executeInCurrentSession(BiConsumer<EntityManager, R> executable, R param) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        executable.accept(entityManager, param);//
        return this;
    }

    synchronized <R> boolean executeInCurrentSession(BiFunction<EntityManager, R, Boolean> executable, R param) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        return executable.apply(entityManager, param);//
    }


    synchronized PersistenceWrapper executeInCurrentSession(Consumer<EntityManager> executable) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        executable.accept(entityManager);
        return this;
    }


    synchronized <R, F> R executeInSessionAndReturn(BiFunction<EntityManager, F, R> function, F param) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        return function.apply(entityManager, param);
    }

    synchronized <R> R executeInSessionAndReturn(Function<EntityManager, R> function) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        return function.apply(entityManager);
    }


    synchronized PersistenceWrapper closeCurrentSession() {
        if (entityManager != null) {
            entityManager.close();
        }
        entityManager = null;
        return this;
    }
}
