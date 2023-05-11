package org.emerald.butler.entity;

import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.TelegramChatBuilder;

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

    @JoinColumn(name = "id_house")
    @OneToOne(fetch = FetchType.LAZY)
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