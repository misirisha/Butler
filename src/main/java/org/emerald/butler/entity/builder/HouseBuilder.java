package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.House;

@RequiredArgsConstructor
public class HouseBuilder {
    private final Metadata metadata;
    private String region;
    private String city;
    private String street;
    private String number;

    public HouseBuilder region(String region) {
        this.region = region;
        return this;
    }

    public HouseBuilder city(String city) {
        this.city = city;
        return this;
    }

    public HouseBuilder street(String street) {
        this.street = street;
        return this;
    }

    public HouseBuilder number(String number) {
        this.number = number;
        return this;
    }

    public House build() {
        House house = metadata.create(House.class);
        house.setRegion(region);
        house.setCity(city);
        house.setStreet(street);
        house.setNumber(number);
        return house;
    }
}
