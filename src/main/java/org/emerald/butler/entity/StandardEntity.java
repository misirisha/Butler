package org.emerald.butler.entity;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Setter
@Getter
@JmixEntity(name = "StandardEntity")
@MappedSuperclass
public abstract class StandardEntity {

    @Id
    @Column(name = "ID")
    @JmixGeneratedValue
    protected UUID id;
}
