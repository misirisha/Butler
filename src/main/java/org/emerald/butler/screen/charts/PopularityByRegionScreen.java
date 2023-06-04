package org.emerald.butler.screen.charts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import io.jmix.charts.component.PieChart;
import io.jmix.core.DataManager;
import io.jmix.core.FluentValuesLoader;
import io.jmix.core.LoadContext;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
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
import org.emerald.butler.component.Profiles;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("PopularityByRegionScreen")
@UiDescriptor("popularity-by-region-screen.xml")
public class PopularityByRegionScreen extends Screen {

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
    private DataGeneration dataGeneration;

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
                        "SELECT h.region as title, COUNT(dar) as amount FROM House h " +
                                "INNER JOIN Apartment a ON a.house = h " +
                                "INNER JOIN DwellerApartmentRole dar ON dar.apartment = a " +
                                "GROUP BY h.region " +
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
                        "SELECT h.region as title, COUNT(a) as amount FROM House h " +
                                "INNER JOIN Apartment a ON a.house = h " +
                                "GROUP BY h.region " +
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

    private void generate() {
        dataGeneration.generate();

        notifications.create(Notifications.NotificationType.HUMANIZED)
                .withPosition(Notifications.Position.BOTTOM_RIGHT)
                .withCaption(messages.getMessage("notification.success.caption"))
                .withDescription(messages.getMessage("notification.success.description"))
                .show();

        updateChartData();
    }
}