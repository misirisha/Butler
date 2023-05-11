package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.House;
import org.emerald.butler.entity.TelegramChat;

@RequiredArgsConstructor
public class TelegramChatBuilder {
    private final Metadata metadata;
    private String telegramChatId;
    private House house;

    public TelegramChatBuilder telegramChatId(Object telegramChatId) {
        this.telegramChatId = telegramChatId == null ? null : telegramChatId.toString();
        return this;
    }

    public TelegramChatBuilder house(House house) {
        this.house = house;
        return this;
    }

    public TelegramChat build() {
        TelegramChat tc = metadata.create(TelegramChat.class);
        tc.setTelegramChatId(telegramChatId);
        tc.setHouse(house);
        return tc;
    }
}
