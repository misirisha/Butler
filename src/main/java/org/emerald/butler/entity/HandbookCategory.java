package org.emerald.butler.entity;

import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JmixEntity
@Table(name = "HANDBOOK_CATEGORY")
@Entity
public class HandbookCategory extends StandardEntity {
    public static final String PHONES = "Телефоны ав. служб";
    public static final String REPAIRS = "Даты проведения рем. работ";
    public static final String MEETINGS = "Общедомовые мероприятия";

    @JoinColumn(name = "HANDBOOK_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Handbook handbook;

    @OneToMany(mappedBy = "category")
    private Collection<HandbookItem> items;

    @InstanceName
    @Column(name = "NAME")
    private String name;
}