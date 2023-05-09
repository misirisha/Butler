package org.emerald.butler.repository;

import java.util.Optional;

import io.jmix.core.repository.JmixDataRepository;
import org.emerald.butler.entity.House;
import org.emerald.butler.entity.TelegramChat;
import org.springframework.stereotype.Repository;

@Repository
public interface TelegramChatRepository extends JmixDataRepository<TelegramChat, Long> {

    Optional<TelegramChat> findByHouse(House house);

}
