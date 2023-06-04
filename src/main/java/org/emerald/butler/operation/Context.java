package org.emerald.butler.operation;

import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.UserCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@RequiredArgsConstructor
public class Context {
    public final String currentStage;
    public final Update update;
    public final User user;
    public final UserCommand userCommand;
    public final String text;
}