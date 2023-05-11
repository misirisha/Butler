package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.ChatRole;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.DwellerChatRole;
import org.emerald.butler.entity.TelegramChat;

@RequiredArgsConstructor
public class DwellerChatRoleBuilder {
    private final Metadata metadata;
    private TelegramChat chat;
    private Dweller dweller;
    private ChatRole chatRole;

    public DwellerChatRoleBuilder chat(TelegramChat chat) {
        this.chat = chat;
        return this;
    }

    public DwellerChatRoleBuilder dweller(Dweller dweller) {
        this.dweller = dweller;
        return this;
    }

    public DwellerChatRoleBuilder chatRole(ChatRole chatRole) {
        this.chatRole = chatRole;
        return this;
    }

    public DwellerChatRole build() {
        DwellerChatRole r = metadata.create(DwellerChatRole.class);
        r.setChat(chat);
        r.setDweller(dweller);
        r.setChatRole(chatRole);
        return r;
    }
}
