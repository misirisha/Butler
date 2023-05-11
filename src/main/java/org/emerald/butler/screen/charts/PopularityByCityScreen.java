package org.emerald.butler.screen.charts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import io.jmix.charts.component.PieChart;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.Stores;
import io.jmix.core.entity.KeyValueEntity;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ComboBox;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.HasValue;
import io.jmix.ui.component.TextField;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.component.Profiles;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.ApartmentRole;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.emerald.butler.entity.House;
import org.emerald.butler.util.RandomList;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("PopularityByCityScreen")
@UiDescriptor("popularity-by-city-screen.xml")
public class PopularityByCityScreen extends Screen {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final long EMPTY_VALUE = 0;
    private static final int MAX_HOUSE_NUMBER = 250;
    private static final int MAX_APARTMENT_NUMBER = 250;
    private static final int MAX_FLOOR_NUMBER = 15;
    private static final String STREET = "ул. Блиновская";
    private static final int MAX_APARTMENTS = 100;
    private static final int MAX_DWELLERS = 6;
    private static final int MAX_HOUSES = 100;

    private final Random random = new Random();

    @Autowired
    private Notifications notifications;
    @Autowired
    private Metadata metadata;
    @Autowired
    private Messages messages;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Profiles profiles;

    @Autowired
    private PieChart pie3DChart;
    @Autowired
    private ComboBox<Mode> modeBox;
    @Autowired
    private TextField<Integer> maxResultsField;

    @Subscribe
    private void onBeforeShow(BeforeShowEvent event) {
        if (profiles.isDebug()) {
            getWindow().add(generateButton(), 0);
        }

        updateChartData();
    }

    @Subscribe
    private void onAfterShow(AfterShowEvent event) {
        modeBox.setValue(Mode.BY_DWELLERS);
    }

    @Subscribe("modeBox")
    private void onModeBoxValueChange(HasValue.ValueChangeEvent<Mode> event) {
        updateChartData();
    }

    @Subscribe("maxResultsField")
    private void onMaxResultsFieldValueChange(HasValue.ValueChangeEvent<Integer> event) {
        updateChartData();
    }

    private Button generateButton() {
        Button button = uiComponents.create(Button.NAME);
        button.setCaption(messages.getMessage("button.generate.caption"));
        button.setAlignment(Component.Alignment.MIDDLE_CENTER);
        button.addClickListener(ignored -> this.generate());
        return button;
    }

    private void updateChartData() {
        pie3DChart.setDataProvider(new ListDataProvider(getDataItems()));
    }

    private List<DataItem> getDataItems() {
        Mode mode = modeBox.getValue();
        if (Objects.isNull(mode)) {
            return new ArrayList<>();
        } else if (mode == Mode.BY_DWELLERS) {
            return getDwellersData();
        } else if (mode == Mode.BY_APARTMENTS) {
            return getApartmentsData();
        } else {
            throw new IllegalStateException("Unknown mode is selected. Selected: " + mode);
        }
    }

    private List<DataItem> getDwellersData() {
        Integer maxResults = maxResultsField.getValue();
        FluentValuesLoader loader = dataManager.loadValues(
                        "SELECT h.city as title, COUNT(dar) as amount FROM House h " +
                                "INNER JOIN Apartment a ON a.house = h " +
                                "INNER JOIN DwellerApartmentRole dar ON dar.apartment = a " +
                                "GROUP BY h.city " +
                                "ORDER BY COUNT(a) DESC"
                )
                .store(Stores.MAIN)
                .properties("title", "amount");

        List<KeyValueEntity> entities;
        if (Objects.isNull(maxResults)) {
            entities = loader.list();
        } else {
            entities = loader
                    .maxResults(maxResults)
                    .list();
        }

        return entities.stream()
                .map(entity -> new MapDataItem(Map.of(
                        "key", entity.getValue("title"),
                        "value", entity.getValue("amount")
                )))
                .collect(Collectors.toList());
    }

    private long allDwellers() {
        LoadContext<DwellerApartmentRole> context = new LoadContext<>(metadata.getClass(DwellerApartmentRole.class));
        context.setQuery(new LoadContext.Query("SELECT dar FROM DwellerApartmentRole dar"));
        return dataManager.getCount(context);
    }

    private List<DataItem> getApartmentsData() {
        List<KeyValueEntity> entities = dataManager.loadValues(
                        "SELECT h.city as title, COUNT(a) as amount FROM House h " +
                                "INNER JOIN Apartment a ON a.house = h " +
                                "GROUP BY h.city " +
                                "ORDER BY COUNT(a) DESC"
                )
                .store(Stores.MAIN)
                .properties("title", "amount")
                .list();

        return entities.stream()
                .map(entity -> new MapDataItem(Map.of(
                        "key", entity.getValue("title"),
                        "value", entity.getValue("amount")
                )))
                .collect(Collectors.toList());
    }

    private long allApartments() {
        LoadContext<Apartment> context = new LoadContext<>(metadata.getClass(Apartment.class));
        context.setQuery(new LoadContext.Query("SELECT a FROM Apartment a"));
        return dataManager.getCount(context);
    }

    private BigDecimal getPercentage(long divided, long divisor) {
        if (divisor == EMPTY_VALUE) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(divided)
                .divide(BigDecimal.valueOf(divisor), 3, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    private void generate() {
        RandomList<CityDto> dtos = new RandomList<>(getDtos());

        SaveContext context = new SaveContext();
        for (int i = 0; i < MAX_HOUSES; i++) {
            CityDto dto = dtos.getRandom();
            House house = metadata.create(House.class);
            house.setCity(dto.getCity());
            house.setRegion(dto.getRegion());
            house.setStreet(STREET);
            house.setNumber(Integer.toString(random.nextInt(MAX_HOUSE_NUMBER - 1) + 1));

            context.saving(house);

            int apartments = random.nextInt(MAX_APARTMENTS - 1) + 1;
            for (int j = 0; j < apartments; j++) {
                Apartment apartment = metadata.create(Apartment.class);
                apartment.setHouse(house);
                apartment.setFloor(random.nextInt(MAX_FLOOR_NUMBER - 1) + 1);
                apartment.setNumber(random.nextInt(MAX_APARTMENT_NUMBER - 1) + 1);

                context.saving(apartment);

                int dwellers = random.nextInt(MAX_DWELLERS - 1) + 1;
                for (int q = 0; q < dwellers; q++) {
                    DwellerApartmentRole dweller = metadata.create(DwellerApartmentRole.class);
                    dweller.setApartment(apartment);
                    dweller.setDweller(null);
                    dweller.setApartmentRole(ApartmentRole.DWELLER);

                    context.saving(dweller);
                }
            }
        }

        dataManager.save(context);

        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withPosition(Notifications.Position.BOTTOM_RIGHT)
                .withCaption(messages.getMessage("notification.success.caption"))
                .withDescription(messages.getMessage("notification.success.description"))
                .show();

        updateChartData();
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