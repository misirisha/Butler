package org.emerald.butler.screen.charts;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.jmix.charts.component.PieChart;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Stores;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiComponents;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.HBoxLayout;
import io.jmix.ui.component.TextField;
import io.jmix.ui.component.VBoxLayout;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.data.DataItem;
import io.jmix.ui.data.impl.ListDataProvider;
import io.jmix.ui.data.impl.MapDataItem;
import io.jmix.ui.icon.JmixIcon;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.component.Profiles;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("PopularityByAgeScreen")
@UiDescriptor("popularity-by-age-screen.xml")
public class PopularityByAgeScreen extends Screen {
    private final Collection<Range> ranges = new ArrayList<>();

    @Autowired
    private DataManager dataManager;
    @Autowired
    private UiComponents uiComponents;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;
    @Autowired
    private DataGeneration dataGeneration;
    @Autowired
    private Profiles profiles;

    @Autowired
    private VBoxLayout rangesBox;
    @Autowired
    private PieChart pie3DChart;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (profiles.isDebug()) {
            getWindow().add(generateButton(), 0);
        }
    }

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {
        ranges.addAll(List.of(
                new Range(uiComponents, ranges::remove, rangesBox, 0, 18).init(),
                new Range(uiComponents, ranges::remove, rangesBox, 19, 35).init(),
                new Range(uiComponents, ranges::remove, rangesBox, 36, 50).init(),
                new Range(uiComponents, ranges::remove, rangesBox, 50, null).init()
        ));
    }

    @Subscribe("launchBtn")
    public void onLaunchBtnClick(Button.ClickEvent event) {
        pie3DChart.setVisible(true);
        pie3DChart.setDataProvider(new ListDataProvider(getItems()));
    }

    @Subscribe("addRangeBtn")
    public void onAddRangeBtnClick(Button.ClickEvent event) {
        final Range range = new Range(uiComponents, ranges::remove, rangesBox);
        range.init();
        ranges.add(range);
    }

    private Button generateButton() {
        Button button = uiComponents.create(Button.NAME);
        button.setCaption(messages.getMessage("button.generate.caption"));
        button.setAlignment(Component.Alignment.MIDDLE_CENTER);
        button.addClickListener(ignored -> this.generate());
        return button;
    }

    private List<DataItem> getItems() {
        final List<DataItem> items = new ArrayList<>();

        final LocalDate now = LocalDate.now();
        for (Range range : ranges) {
            final String title = range.getMin().orElse(0) + " - " + range.getMax().map(Object::toString).orElse("?");
            final LocalDate from = now.minusYears(range.getMax().orElse(1_000));
            final LocalDate until = now.minusYears(range.getMin().orElse(0));
            final List<DataItem> ranged = dataManager.loadValues("SELECT COUNT(tu) as humans FROM Dweller tu " +
                            "WHERE tu.birthDate BETWEEN :startDate AND :endDate")
                    .parameter("startDate", from)
                    .parameter("endDate", until)
                    .store(Stores.MAIN)
                    .properties("humans")
                    .list()
                    .stream()
                    .map(entity -> new MapDataItem(Map.of(
                            "key", title,
                            "value", entity.getValue("humans")
                    )))
                    .collect(Collectors.toList());

            items.addAll(ranged);
        }

        return items;
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

    private void updateChartData() {
        pie3DChart.setDataProvider(new ListDataProvider(getItems()));
    }

    @RequiredArgsConstructor
    private static final class Range {
        private final UiComponents uiComponents;
        private final Consumer<Range> onDelete;
        private final ComponentContainer container;
        private final UUID id = UUID.randomUUID();

        private Integer initialFrom;
        private Integer initialUntil;

        private TextField<Integer> fromField;
        private TextField<Integer> untilField;
        private Button deleteBtn;

        public Range(UiComponents uiComponents,
                     Consumer<Range> onDelete,
                     ComponentContainer container,
                     Integer initialFrom,
                     Integer initialUntil) {
            this.uiComponents = uiComponents;
            this.onDelete = onDelete;
            this.container = container;
            this.initialFrom = initialFrom;
            this.initialUntil = initialUntil;
        }

        public Range init() {
            fromField = uiComponents.create(TextField.TYPE_INTEGER);
            fromField.addValidator(value -> {
                final Integer max = untilField.getValue();
                if (Objects.isNull(max)) {
                    if (value < 0) {
                        throw new ValidationException("Значение должно быть положительным");
                    }
                    return;
                }

                if (max.compareTo(value) < 0) {
                    throw new ValidationException("Максимальное значение должно быть меньше, чем минимальное");
                }
            });
            fromField.setAlignment(Component.Alignment.MIDDLE_CENTER);

            untilField = uiComponents.create(TextField.TYPE_INTEGER);
            untilField.addValidator(value -> {
                final Integer min = fromField.getValue();
                if (Objects.isNull(min)) {
                    if (value < 0) {
                        throw new ValidationException("Значение должно быть положительным");
                    }
                    return;
                }

                if (min.compareTo(value) > 0) {
                    throw new ValidationException("Минимальное значение должно быть больше, чем максимальное");
                }
            });
            untilField.setAlignment(Component.Alignment.MIDDLE_CENTER);

            final HBoxLayout layout = uiComponents.create(HBoxLayout.NAME);
            deleteBtn = uiComponents.create(Button.class);
            deleteBtn.addClickListener(event -> {
                onDelete.accept(this);
                container.remove(layout);
            });
            deleteBtn.setAlignment(Component.Alignment.MIDDLE_CENTER);
            deleteBtn.setIconFromSet(JmixIcon.REMOVE);

            layout.add(fromField, untilField, deleteBtn);
            layout.setSpacing(true);
            layout.setAlignment(Component.Alignment.MIDDLE_LEFT);
            container.add(layout);

            Optional.ofNullable(this.initialFrom).ifPresent(i -> fromField.setValue(i));
            Optional.ofNullable(this.initialUntil).ifPresent(i -> untilField.setValue(i));

            return this;
        }

        public Optional<Integer> getMin() {
            return Optional.ofNullable(fromField.getValue());
        }

        public Optional<Integer> getMax() {
            return Optional.ofNullable(untilField.getValue());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Range range = (Range) o;
            return Objects.equals(id, range.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }
}