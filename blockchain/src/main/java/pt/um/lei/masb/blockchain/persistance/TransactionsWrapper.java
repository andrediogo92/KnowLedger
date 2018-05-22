package pt.um.lei.masb.blockchain.persistance;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

interface TransactionsWrapper {


    /**
     * Not to be used directly. Requires knowledge of inner workings of DB.
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param namedQuery    The query's name.
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @return An optional result (querying might fail).
     */
    default <T> Optional<T> getUniqueResult(Class<T> tClass,
                                            EntityManager entityManager,
                                            String namedQuery,
                                            Logger logger) {
        Optional<T> res = Optional.empty();
        try {
            var query = entityManager.createNamedQuery(namedQuery, tClass);
            res = Optional.of(query.getSingleResult());
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;
    }


    /**
     * Not to be used directly. Requires knowledge of inner workings of DB.
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param namedQuery    The query's name.
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @return A list that might be null (querying might fail).
     */
    default <T> List<T> getResults(Class<T> tClass,
                                   EntityManager entityManager,
                                   String namedQuery,
                                   Logger logger) {
        List<T> res = null;
        try {
            var query = entityManager.createNamedQuery(namedQuery, tClass);
            res = query.getResultList();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;
    }


    /**
     * Not to be used directly. Requires knowledge of inner workings of DB.
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param namedQuery    The query's name.
     * @param paramName     The parameter name to fill in in the query.
     * @param param         The actual parameter to fill
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @param <R>           The type of the parameter supplied.
     * @return An optional result (querying might fail).
     */
    default <T, R> Optional<T> getUniqueResultSingleParameter(Class<T> tClass,
                                                              EntityManager entityManager,
                                                              String namedQuery,
                                                              String paramName,
                                                              R param,
                                                              Logger logger) {
        Optional<T> res = Optional.empty();
        try {
            var query = entityManager.createNamedQuery(namedQuery, tClass);
            query.setParameter(paramName, param);
            res = Optional.of(query.getSingleResult());
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;
    }


    /**
     * Not to be used directly. Requires knowledge of inner workings of DB.
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param namedQuery    The query's name.
     * @param paramMap      A map of parameter names and corresponding parameter to feed the query.
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @param <R>           The type of the parameter supplied.
     * @return An optional result (querying might fail).
     */
    default <T, R> Optional<T> getUniqueResultMultiParameter(Class<T> tClass,
                                                             EntityManager entityManager,
                                                             String namedQuery,
                                                             Map<String, R> paramMap,
                                                             Logger logger) {
        Optional<T> res = Optional.empty();
        try {
            var query = entityManager.createNamedQuery(namedQuery, tClass);
            paramMap.forEach(query::setParameter);
            res = Optional.of(query.getSingleResult());
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;
    }

    /**
     * Not to be used directly. Requires knowledge of inner workings of DB.
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param namedQuery    The query's name.
     * @param paramName     The parameter name to fill in in the query.
     * @param param         The actual parameter to fill
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @param <R>           The type of the parameter supplied.
     * @return A list that might be null (querying might fail).
     */
    default <T, R> List<T> getResultsSingleParameter(Class<T> tClass,
                                                     EntityManager entityManager,
                                                     String namedQuery,
                                                     String paramName,
                                                     R param,
                                                     Logger logger) {
        List<T> res = null;
        try {
            var query = entityManager.createNamedQuery(namedQuery, tClass);
            query.setParameter(paramName, param);
            res = query.getResultList();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;

    }

    /**
     * Not to be used directly. Requires knowledge of inner workings of DB.
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param namedQuery    The query's name.
     * @param paramMap      A map of parameter names and corresponding parameter to feed the query.
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @param <R>           The type of the parameter supplied.
     * @return A list that might be null (querying might fail).
     */

    default <T, R> List<T> getResultsMultiParameter(Class<T> tClass,
                                                    EntityManager entityManager,
                                                    String namedQuery,
                                                    Map<String, R> paramMap,
                                                    Logger logger) {
        List<T> res = null;
        try {
            var query = entityManager.createNamedQuery(namedQuery, tClass);
            paramMap.forEach(query::setParameter);
            res = query.getResultList();
        } catch (Exception e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;
    }

    /**
     * Not to be used directly. Requires knowledge of inner workings of DB:
     *
     * @param tClass        The class of the object result.
     * @param entityManager The entity manager to query.
     * @param id            The unique id of the object.
     * @param logger        A logger to log exceptional flows.
     * @param <T>           The type of the result queried.
     * @param <R>           The type of the id supplied.
     * @return An optional result (querying might fail).
     */
    default <T, R> Optional<T> findEntity(Class<T> tClass,
                                          EntityManager entityManager,
                                          R id,
                                          Logger logger) {
        Optional<T> res = Optional.empty();
        try {
            res = Optional.ofNullable(entityManager.find(tClass, id));
        } catch (IllegalArgumentException e) {
            logger.log(Level.WARNING, e.getMessage());
        }
        return res;
    }

    default <T> boolean persistEntity(EntityManager entityManager,
                                      T param,
                                      Logger logger) {
        try {
            entityManager.persist(param);
            return true;
        } catch (IllegalArgumentException |
                EntityExistsException |
                TransactionRequiredException e) {
            logger.log(Level.SEVERE, e::getMessage);
        }
        return false;
    }

    default <T> boolean mergeEntity(EntityManager entityManager,
                                    T param,
                                    Logger logger) {
        try {
            entityManager.merge(param);
            return true;
        } catch (IllegalArgumentException |
                TransactionRequiredException e) {
            logger.log(Level.SEVERE, e::getMessage);
        }
        return false;
    }

    /**
     * Should be called when querying is finished to reset the persistence context.
     */
    void clearTransactionsContext();

}
