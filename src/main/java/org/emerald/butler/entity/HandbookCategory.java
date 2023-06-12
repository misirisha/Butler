package org.emerald.butler.entity;

import io.jmix.core.DeletePolicy;
import io.jmix.core.entity.annotation.OnDeleteInverse;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Collection;

@Setter
@Getter
@JmixEntity
@Table(name = "HANDBOOK_CATEGORY")
@Entity
public class HandbookCategory extends StandardEntity {
    public static final String PHONES = "Телефоны ав. служб";

    public static final String REPAIRS = "Даты проведения рем. работ";

    public static final String MEETINGS = "Общедомовые мероприятия";

    @OnDeleteInverse(DeletePolicy.CASCADE)
    @JoinColumn(name = "HANDBOOK_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Handbook handbook;

    @OneToMany(mappedBy = "category")
    private Collection<HandbookItem> items;

    @InstanceName
    @Column(name = "NAME")
    private String name;
}