package org.emerald.butler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Entity(name = "House")
@Table(name = "House")
public class House extends StandardEntity {

    @Column(name ="region")
    private String region;

    @Column(name ="city")
    private String city;

    @Column(name ="street")
    private String street;

    @Column(name ="number")
    private String number;
}
