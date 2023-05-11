package org.emerald.butler.entity.builder;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.ApartmentRole;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.DwellerApartmentRole;

@RequiredArgsConstructor
public class DwellerApartmentRoleBuilder {
    private final Metadata metadata;
    private Apartment apartment;
    private Dweller dweller;
    private ApartmentRole apartmentRole;

    public DwellerApartmentRoleBuilder apartment(Apartment apartment) {
        this.apartment = apartment;
        return this;
    }

    public DwellerApartmentRoleBuilder dweller(Dweller dweller) {
        this.dweller = dweller;
        return this;
    }

    public DwellerApartmentRoleBuilder apartmentRole(ApartmentRole apartmentRole) {
        this.apartmentRole = apartmentRole;
        return this;
    }

    public DwellerApartmentRole build() {
        DwellerApartmentRole r = metadata.create(DwellerApartmentRole.class);
        r.setApartment(apartment);
        r.setDweller(dweller);
        r.setApartmentRole(apartmentRole);
        return r;
    }
}
