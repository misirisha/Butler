package org.emerald.butler.repository.impl;

import java.util.function.Consumer;
import javax.persistence.EntityManager;

import io.jmix.core.Stores;
import io.jmix.data.StoreAwareLocator;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.repository.TransportRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RequiredArgsConstructor
@Repository
public class TransportRepositoryImpl implements TransportRepository {
    private final StoreAwareLocator locator;

    @Override
    public void updateOrderByDwellerAndStartOrder(Dweller dweller, Long startOrder) {
        withTransaction(manager ->
                manager.createQuery("UPDATE Transport t SET t.order = t.order - 1 " +
                                "WHERE t.dweller = :dweller AND t.order > :startOrder")
                        .setParameter("dweller", dweller)
                        .setParameter("startOrder", startOrder)
                        .executeUpdate()
        );
    }

    private void withTransaction(Consumer<EntityManager> consumer) {
        PlatformTransactionManager transactionManager = locator.getTransactionManager(Stores.MAIN);
        var transaction = transactionManager.getTransaction(
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        );

        try {
            consumer.accept(locator.getEntityManager(Stores.MAIN));
            transactionManager.commit(transaction);
        } catch (Exception exception) {
            transactionManager.rollback(transaction);
            throw exception;
        }
    }
}
