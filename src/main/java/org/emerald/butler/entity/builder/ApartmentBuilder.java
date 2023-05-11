package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.House;

@RequiredArgsConstructor
public class ApartmentBuilder {
    private final Metadata metadata;
    private House house;
    private Integer frontDoor;
    private Integer floor;
    private Integer number;

    public ApartmentBuilder house(House house) {
        this.house = house;
        return this;
    }

    public ApartmentBuilder frontDoor(Integer frontDoor) {
        this.frontDoor = frontDoor;
        return this;
    }

    public ApartmentBuilder floor(Integer floor) {
        this.floor = floor;
        return this;
    }

    public ApartmentBuilder number(Integer number) {
        this.number = number;
        return this;
    }

    public Apartment build() {
        Apartment apartment = metadata.create(Apartment.class);
        apartment.setHouse(house);
        apartment.setFrontDoor(frontDoor);
        apartment.setFloor(floor);
        apartment.setNumber(number);
        return apartment;
    }
}
