package org.emerald.butler.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.Transport;
import org.springframework.stereotype.Repository;

@Repository
public interface TransportJmixRepository extends JmixDataRepository<Transport, Long> {
    Optional<Transport> findByDwellerAndOrder(Dweller dweller, Long order);

    Collection<Transport> findAllByDwellerAndNumber(Dweller dweller, String number);

    Optional<Transport> findFirstByDwellerOrderByOrderDesc(Dweller dweller);

    List<Transport> findAllByDwellerTelegramId(Long telegramId);

    List<Transport> findAllByDweller(Dweller dweller);

    void deleteByDwellerTelegramIdAndNumber(Long telegramId, String number);
}
