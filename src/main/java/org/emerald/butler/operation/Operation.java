package org.emerald.butler.operation;

import org.emerald.butler.entity.Command;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Operation {

    Command getCommand();
    boolean supports(Command command);
    void doOperation(Update update);


}
