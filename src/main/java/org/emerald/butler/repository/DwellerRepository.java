package org.emerald.butler.repository;

import java.util.Optional;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.Dweller;
import org.springframework.stereotype.Repository;

@Repository
public interface DwellerRepository extends JmixDataRepository<Dweller, Long> {
    Optional<Dweller> findByTelegramId(Long telegramId);
}
