package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Dweller;

@RequiredArgsConstructor
public class DwellerBuilder {
    private final Metadata metadata;
    private Long telegramId;
    private String firstName;
    private String lastName;
    private String userName;

    public DwellerBuilder telegramId(Long telegramId) {
        this.telegramId = telegramId;
        return this;
    }

    public DwellerBuilder firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public DwellerBuilder lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public DwellerBuilder userName(String userName) {
        this.userName = userName;
        return this;
    }

    public Dweller build() {
        Dweller dweller = metadata.create(Dweller.class);
        dweller.setTelegramId(telegramId);
        dweller.setFirstName(firstName);
        dweller.setLastName(lastName);
        dweller.setUserName(userName);
        return dweller;
    }
}
