package org.emerald.butler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "DwellerChatRole")
@Table(name = "Dweller_chat_role")
public class DwellerChatRole extends StandardEntity {

    @JoinColumn(name = "id_chat")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelegramChat chat;

    @JoinColumn(name = "id_dweller")
    @ManyToOne(fetch = FetchType.LAZY)
    private Dweller dweller;

    @Column(name = "chat_role")
    private String chatRole;

    public ChatRole getChatRole() {
        return chatRole == null ? null : ChatRole.fromId(chatRole);
    }

    public void setChatRole(ChatRole chatRole) {
        this.chatRole = chatRole == null ? null : chatRole.getId();
    }
}
