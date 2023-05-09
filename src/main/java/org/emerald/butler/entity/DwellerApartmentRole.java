package org.emerald.butler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import io.jmix.core.Messages;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
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
@Entity(name = "DwellerApartmentRole")
@Table(name = "Dweller_apartment_role")
public class DwellerApartmentRole extends StandardEntity {

    @JoinColumn(name = "id_apartment")
    @ManyToOne(fetch = FetchType.LAZY)
    private Apartment apartment;

    @JoinColumn(name = "id_dweller")
    @ManyToOne(fetch = FetchType.LAZY)
    private Dweller dweller;

    @Column(name = "apartment_role")
    private String apartmentRole;

    public ApartmentRole getApartmentRole() {
        return apartmentRole == null ? null : ApartmentRole.fromId(apartmentRole);
    }

    public void setApartmentRole(ApartmentRole apartmentRole) {
        this.apartmentRole = apartmentRole == null ? null : apartmentRole.getId();
    }

    @InstanceName
    @DependsOnProperties({"id"})
    public String getInstanceName(Messages messages) {
        return String.format("%s-%s", messages.getMessage(DwellerApartmentRole.class, "DwellerApartmentRole"), getId());
    }
}
