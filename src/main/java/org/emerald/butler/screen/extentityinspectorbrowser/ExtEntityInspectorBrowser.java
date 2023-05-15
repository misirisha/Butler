package org.emerald.butler.screen.extentityinspectorbrowser;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.jmix.ui.action.Action;
import io.jmix.ui.action.list.BulkEditAction;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ButtonsPanel;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import io.jmix.datatoolsui.screen.entityinspector.EntityInspectorBrowser;

@UiController("ext_entityInspector.browse")
@UiDescriptor("ext-entity-inspector-browser.xml")
public class ExtEntityInspectorBrowser extends EntityInspectorBrowser {
    private static final String RESTORE_ACTION_ID = "restore";
    private static final String WIPE_OUT_ACTION_ID = "wipeOut";

    @Override
    protected void createButtonsPanel(Table table) {
        super.createButtonsPanel(table);
        ButtonsPanel panel = table.getButtonsPanel();
        Optional.ofNullable(panel).ifPresent(checkedPanel ->
                Stream.of(findRestoreButton(checkedPanel), findBulkEditButton(checkedPanel), findWipeOutButton(checkedPanel))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach(checkedPanel::remove));
    }

    private Optional<? extends Component> findRestoreButton(ButtonsPanel panel) {
        return panel.getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> {
                    Action action = button.getAction();
                    return Objects.nonNull(action) && RESTORE_ACTION_ID.equals(action.getId());
                })
                .findFirst();
    }

    private Optional<? extends Component> findBulkEditButton(ButtonsPanel panel) {
        return panel.getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> {
                    Action action = button.getAction();
                    return Objects.nonNull(action) && action instanceof BulkEditAction;
                })
                .findFirst();
    }

    private Optional<? extends Component> findWipeOutButton(ButtonsPanel panel) {
        return panel.getComponents().stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> {
                    Action action = button.getAction();
                    return Objects.nonNull(action) && WIPE_OUT_ACTION_ID.equals(action.getId());
                })
                .findFirst();
    }
}