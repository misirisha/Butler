package org.emerald.butler.operation;

import org.emerald.butler.component.Sender;
import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.Command;
import org.emerald.butler.repository.DwellerRepository;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

@Component
public class RequestAddToApartment extends AbstractOperation{

    public RequestAddToApartment(UserCommandManager userCommandManager, Sender sender, DwellerRepository dwellerRepository) {
        super(userCommandManager, sender, dwellerRepository);
    }

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return Map.of(
                "start", this::onStart
        );
    }

    private void onStart(Context context) {

    }

    @Override
    public Command getCommand() {
        return null;
    }

    @Override
    public boolean supports(Command command) {
        return false;
    }
}
