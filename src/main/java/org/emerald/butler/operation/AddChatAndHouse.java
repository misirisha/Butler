package org.emerald.butler.operation;

import java.util.Map;
import java.util.function.Consumer;

import org.emerald.butler.component.Sender;
import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.Command;
import org.emerald.butler.repository.DwellerRepository;

public class AddChatAndHouse extends AbstractOperation {

    public AddChatAndHouse(UserCommandManager userCommandManager, Sender sender, DwellerRepository dwellerRepository) {
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
        return Command.ADD_CHAT_AND_HOUSE;
    }

    @Override
    public boolean supports(Command command) {
        return command == Command.ADD_CHAT_AND_HOUSE;
    }
}
