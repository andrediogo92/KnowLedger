package pt.um.lei.masb.blockchain.persistance;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.function.Consumer;
import java.util.function.Function;

public class PersistanceWrapper {
    private static final PersistanceWrapper persistanceWrapper = new PersistanceWrapper();

    private final EntityManagerFactory sessionFactory;
    private EntityManager entityManager;

    private PersistanceWrapper() {
        sessionFactory = Persistence.createEntityManagerFactory("pt.um.lei.masb.blockchain.unit");
    }

    public static PersistanceWrapper getInstance() {
        return persistanceWrapper;
    }

    public PersistanceWrapper executeInCurrentSession(Consumer<EntityManager> executable) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        executable.accept(entityManager);
        return this;
    }

    public <R> R executeInSessionAndReturn(Function<EntityManager, R> function) {
        if (entityManager == null) {
            entityManager = sessionFactory.createEntityManager();
        }
        return function.apply(entityManager);
    }


    public PersistanceWrapper closeCurrentSession() {
        if (entityManager != null) {
            entityManager.close();
        }
        entityManager = null;
        return this;
    }
}
