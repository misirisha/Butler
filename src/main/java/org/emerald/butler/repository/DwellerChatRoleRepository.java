package org.emerald.butler.repository;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.DwellerChatRole;
import org.springframework.stereotype.Repository;

@Repository
public interface DwellerChatRoleRepository extends JmixDataRepository<DwellerChatRole, Long> {
}
