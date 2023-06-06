package org.emerald.butler.repository;

import java.util.Optional;
import java.util.UUID;

import io.jmix.core.FetchPlan;
import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.Handbook;

public interface HandbookRepository extends JmixDataRepository<Handbook, UUID> {
    Optional<Handbook> findByChatTelegramChatId(FetchPlan plan, String id);
}
