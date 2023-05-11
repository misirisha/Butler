package org.emerald.butler.repository;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.DwellerChatRole;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DwellerChatRoleRepository extends JmixDataRepository<DwellerChatRole, Long> {
    List<DwellerChatRole> findAllByDwellerTelegramId(Long telegramId);
}
