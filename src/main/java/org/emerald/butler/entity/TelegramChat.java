package org.emerald.butler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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

    @Column(name = "telegram_chat_id")
    private String telegramChatId;

    @JoinColumn(name = "id_house")
    @OneToOne(fetch = FetchType.LAZY)
    private House house;

    public static TelegramChatBuilder builder(Metadata metadata) {
        return new TelegramChatBuilder(metadata);
    }

    @InstanceName
    @DependsOnProperties({"id"})
    public String getInstanceName(Messages messages) {
        return String.format("%s-%s", messages.getMessage(TelegramChat.class, "TelegramChat"), getId());
    }
}
