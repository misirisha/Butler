package org.emerald.butler.entity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
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
@Entity(name = "TelegramChat")
@Table(name = "TelegramChat")
public class TelegramChat extends StandardEntity {

    @JoinColumn(name = "telegram_chat_id")
    private String telegramChatId;

    @JoinColumn(name = "id_house")
    @OneToOne(fetch = FetchType.LAZY)
    private House house;
}
