package org.emerald.butler.service;

import java.util.Optional;

import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.TelegramChat;
import org.emerald.butler.repository.TelegramChatRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TelegramChatService {
    private final TelegramChatRepository telegramChatRepository;
    private final Metadata metadata;
    private final DataManager dataManager;

    public TelegramChat getOrCreate(Long id) {
        final Optional<TelegramChat> optional = telegramChatRepository.findByTelegramChatId(id.toString());
        if (optional.isPresent()) {
            return optional.get();
        }

        return dataManager.save(create(id));
    }

    private TelegramChat create(Long id) {
        final TelegramChat chat = metadata.create(TelegramChat.class);
        chat.setTelegramChatId(id.toString());
        return chat;
    }
}
