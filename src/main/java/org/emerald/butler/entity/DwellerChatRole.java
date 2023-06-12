package org.emerald.butler.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.OnDelete;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.DwellerChatRoleBuilder;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "DwellerChatRole")
@Table(name = "Dweller_chat_role")
public class DwellerChatRole extends StandardEntity {

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "id_chat")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelegramChat chat;

    @JoinColumn(name = "id_dweller")
    @ManyToOne(fetch = FetchType.LAZY)
    private Dweller dweller;

    @Column(name = "chat_role")
    private String chatRole;

    public static DwellerChatRoleBuilder builder(Metadata metadata) {
        return new DwellerChatRoleBuilder(metadata);
    }

    public ChatRole getChatRole() {
        return chatRole == null ? null : ChatRole.fromId(chatRole);
    }

    public void setChatRole(ChatRole chatRole) {
        this.chatRole = chatRole == null ? null : chatRole.getId();
    }

    @InstanceName
    @DependsOnProperties({"id"})
    public String getInstanceName(Messages messages) {
        return String.format("%s-%s", messages.getMessage(DwellerChatRole.class, "DwellerChatRole"), getId());
    }
}
