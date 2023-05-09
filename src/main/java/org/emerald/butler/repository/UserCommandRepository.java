package org.emerald.butler.repository;

import java.util.Optional;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.UserCommand;
import org.springframework.stereotype.Repository;

@Repository
public interface UserCommandRepository extends JmixDataRepository<UserCommand, Long> {

    Optional<UserCommand> findByDwellerTelegramId(Long telegramId);

    void deleteByDwellerTelegramId(Long telegramId);
}
