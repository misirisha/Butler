package org.emerald.butler.entity;

import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.emerald.butler.entity.builder.HouseBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JmixEntity
@Entity(name = "House")
@Table(name = "House")
public class House extends StandardEntity {

    @OneToMany(mappedBy = "house")
    private Collection<Apartment> apartmens;

    @Column(name = "region")
    private String region;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name = "number")
    private String number;

    public static HouseBuilder builder(Metadata metadata) {
        return new HouseBuilder(metadata);
    }

    public Collection<Apartment> getApartmens() {
        return apartmens;
    }

    public void setApartmens(Collection<Apartment> apartmens) {
        this.apartmens = apartmens;
    }

    @InstanceName
    @DependsOnProperties({"region", "city", "street", "number"})
    public String getInstanceName() {
        return String.format("%s %s %s %s", region, city, street, number);
    }

    @Override
    public String toString() {
        return getRegion() + ", " + getCity()+ ", " + getStreet() + ", " + getNumber();
    }
}