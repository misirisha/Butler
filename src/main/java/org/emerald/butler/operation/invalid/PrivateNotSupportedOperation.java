package org.emerald.butler.operation.invalid;

import lombok.RequiredArgsConstructor;
import org.emerald.butler.component.Sender;
import org.emerald.butler.entity.Command;
import org.emerald.butler.operation.Operation;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
@Component
public class PrivateNotSupportedOperation implements Operation {
    private final Sender sender;

    @Override
    public Command getCommand() {
        return null;
    }

    @Override
    public boolean supports(Command command) {
        return false;
    }

    @Override
    public void doOperation(Update update) {
        sender.send("Текущая операция не поддерживается в личных сообщениях.", update);
    }
}
