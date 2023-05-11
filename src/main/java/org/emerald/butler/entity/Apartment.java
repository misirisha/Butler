package org.emerald.butler.entity;

import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.ApartmentBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "Apartment")
@Table(name = "Apartment")
public class Apartment extends StandardEntity {

    @JoinColumn(name = "id_house")
    @ManyToOne(fetch = FetchType.LAZY)
    private House house;

    @OneToMany(mappedBy = "apartment")
    private Collection<DwellerApartmentRole> dwellers;

    @Column(name = "front_door")
    private Integer frontDoor;

    @Column(name = "floor")
    private Integer floor;

    @InstanceName
    @Column(name = "number")
    private Integer number;

    public Collection<DwellerApartmentRole> getDwellers() {
        return dwellers;
    }

    public void setDwellers(Collection<DwellerApartmentRole> dwellers) {
        this.dwellers = dwellers;
    }

    public static ApartmentBuilder builder(Metadata metadata) {
        return new ApartmentBuilder(metadata);
    }
}