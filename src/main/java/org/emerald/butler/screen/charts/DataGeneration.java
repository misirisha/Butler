package org.emerald.butler.screen.charts;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.persistence.EntityManager;

import io.jmix.core.Metadata;
import io.jmix.core.Stores;
import io.jmix.data.StoreAwareLocator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.ApartmentRole;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.emerald.butler.entity.House;
import org.emerald.butler.entity.StandardEntity;
import org.emerald.butler.util.RandomList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RequiredArgsConstructor
@Service
public class DataGeneration {
    private static final int MAX_HOUSE_NUMBER = 250;
    private static final int MAX_APARTMENT_NUMBER = 250;
    private static final int MAX_FLOOR_NUMBER = 15;
    private static final String STREET = "ул. Блиновская";
    private static final int MAX_APARTMENTS = 10;
    private static final int MAX_DWELLERS_IN_APARTMENT = 6;
    private static final int MAX_HOUSES = 10;
    private static final int MAX_DWELLERS = 250;

    private final Random random = new Random();
    private final Metadata metadata;
    private final StoreAwareLocator locator;

    public void generate() {
        final PlatformTransactionManager txManager = locator.getTransactionManager(Stores.MAIN);
        final TransactionStatus status = txManager.getTransaction(new DefaultTransactionDefinition());

        final EntityManager manager = locator.getEntityManager(Stores.MAIN);
        try {
            generate_().forEach(manager::persist);

            txManager.commit(status);
        } catch (RuntimeException exception) {
            txManager.rollback(status);
            throw exception;
        }
    }

    private Collection<? extends StandardEntity> generate_() {
        RandomList<CityDto> dtos = new RandomList<>(getDtos());

        final Collection<StandardEntity> toSave = new ArrayList<>();
        final RandomList<Dweller> dwellersList = new RandomList<>();
        for (int i = 0; i < MAX_DWELLERS; i++) {
            final Dweller dweller = createDweller();
            toSave.add(dweller);

            dwellersList.add(dweller);
        }

        for (int i = 0; i < MAX_HOUSES; i++) {
            CityDto dto = dtos.getRandom();
            House house = metadata.create(House.class);
            house.setCity(dto.getCity());
            house.setRegion(dto.getRegion());
            house.setStreet(STREET);
            house.setNumber(Integer.toString(random.nextInt(MAX_HOUSE_NUMBER - 1) + 1));

            toSave.add(house);

            int apartments = random.nextInt(MAX_APARTMENTS - 1) + 1;
            for (int j = 0; j < apartments; j++) {
                Apartment apartment = metadata.create(Apartment.class);
                apartment.setHouse(house);
                apartment.setFloor(random.nextInt(MAX_FLOOR_NUMBER - 1) + 1);
                apartment.setNumber(random.nextInt(MAX_APARTMENT_NUMBER - 1) + 1);

                toSave.add(apartment);

                int dwellers = random.nextInt(MAX_DWELLERS_IN_APARTMENT - 1) + 1;
                for (int q = 0; q < dwellers; q++) {
                    DwellerApartmentRole dweller = metadata.create(DwellerApartmentRole.class);
                    dweller.setApartment(apartment);
                    dweller.setDweller(dwellersList.getRandom());
                    dweller.setApartmentRole(ApartmentRole.DWELLER);

                    toSave.add(dweller);
                }
            }
        }

        return toSave;
    }

    private Dweller createDweller() {
        final Dweller dweller = metadata.create(Dweller.class);
        dweller.setFirstName("Петя");
        dweller.setLastName("Гланц");
        dweller.setUserName("petia");
        dweller.setBirthDate(generateRandomDate(LocalDate.of(1901, Month.JANUARY, 1), LocalDate.now()));

        return dweller;
    }

    private LocalDate generateRandomDate(LocalDate start, LocalDate end) {
        long startEpochDay = start.toEpochDay();
        long endEpochDay = end.toEpochDay();
        long randomEpochDay = ThreadLocalRandom.current().nextLong(startEpochDay, endEpochDay);
        return LocalDate.ofEpochDay(randomEpochDay);
    }

    private static List<CityDto> getDtos() {
        return List.of(
                new CityDto("Москва", "Москва"),
                new CityDto("Санкт-Петербург", "Санкт-Петербург"),
                new CityDto("Новосибирская область", "Новосибирск"),
                new CityDto("Свердловская область", "Екатеринбург"),
                new CityDto("Татарстан", "Казань"),
                new CityDto("Нижегородская область", "Нижний Новгород"),
                new CityDto("Челябинская область", "Челябинск"),
                new CityDto("Красноярский край", "Красноярск"),
                new CityDto("Самарская область", "Самара"),
                new CityDto("Башкортостан", "Уфа"),
                new CityDto("Ростовская область", "Ростов"),
                new CityDto("Омская область", "Омск"),
                new CityDto("Краснодарский край", "Краснодар"),
                new CityDto("Воронежская область", "Воронеж"),
                new CityDto("Пермский край", "Пермь"),
                new CityDto("Волгоградская область", "Волгоград"),
                new CityDto("Саратовская область", "Саратов"),
                new CityDto("Тюменская область", "Тюмень"),
                new CityDto("Самарская область", "Тольятти"),
                new CityDto("Алтайский край", "Барнаул")
        );
    }

    @RequiredArgsConstructor
    @Getter
    private static class CityDto {
        private final String region;
        private final String city;
    }
}
