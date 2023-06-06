package org.emerald.butler.entity;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JmixEntity
@Table(name = "HANDBOOK")
@Entity
public class Handbook extends StandardEntity {

    @JoinColumn(name = "CHAT_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private TelegramChat chat;

    @OneToMany(mappedBy = "handbook")
    private Collection<HandbookCategory> categories;

    public HandbookCategory getCategoryByName(String name) {
        return findCategoryByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Not found a category with given name. Given: " + name));
    }

    public Optional<HandbookCategory> findCategoryByName(String name) {
        for (HandbookCategory category : categories) {
            if (Objects.equals(category.getName(), name)) {
                return Optional.of(category);
            }
        }

        return Optional.empty();
    }
}