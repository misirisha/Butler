package org.emerald.butler.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.TelegramChatBuilder;

import javax.persistence.*;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "TelegramChat")
@Table(name = "TelegramChat")
public class TelegramChat extends StandardEntity {

    @OneToMany(mappedBy = "chat")
    private Collection<DwellerChatRole> dwellers;

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "id_house")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private House house;

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    public static TelegramChatBuilder builder(Metadata metadata) {
        return new TelegramChatBuilder(metadata);
    }

    public Collection<DwellerChatRole> getDwellers() {
        return dwellers;
    }

    public void setDwellers(Collection<DwellerChatRole> dwellers) {
        this.dwellers = dwellers;
    }

    @InstanceName
    @DependsOnProperties({"id"})
    public String getInstanceName(Messages messages) {
        return String.format("%s-%s", messages.getMessage(TelegramChat.class, "TelegramChat"), getId());
    }
}