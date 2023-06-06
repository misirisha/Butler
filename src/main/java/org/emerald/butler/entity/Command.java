package org.emerald.butler.entity;

import java.util.Optional;
import java.util.Set;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Command {
    ABOUT("1", "Информация обо мне", "/about", Set.of(CommandType.PRIVATE)),
    FIND_HOUSE("2", "Найти дом", "/findhouse", Set.of(CommandType.PRIVATE)),
    ADD_CHAT_AND_HOUSE("3", "Добавить чат и дом", "/addhouse", Set.of(CommandType.GROUP, CommandType.SUPERGROUP)),
    ADD_APARTMENT("4", "Добавить квартиру", "/addapartment", Set.of(CommandType.PRIVATE, CommandType.GROUP, CommandType.SUPERGROUP)),
    FIND_DWELLER("5", "Найти жителя", "/finddweller", Set.of(CommandType.PRIVATE)),
    HANDBOOK("6", "Справочник", "/handbook", Set.of(CommandType.GROUP, CommandType.SUPERGROUP));

    private final String id;
    private final String text;
    private final String telegramCommand;
    private final Set<CommandType> commandTypes;

    public static Command getById(Object id) {
        return findById(id).orElseThrow();
    }

    public static Optional<Command> findById(Object id) {
        for (Command command : Command.values()) {
            if (command.getId().equals(id)) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }

    public static Optional<Command> findByText(Object text) {
        for (Command command : Command.values()) {
            if (command.getText().equals(text)) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }

    public static Optional<Command> findByTelegramCommand(Object text) {
        for (Command command : Command.values()) {
            if (command.getTelegramCommand().equals(text)) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }

    public boolean supports(CommandType type) {
        return commandTypes.contains(type);
    }

    public boolean doesNotSupport(CommandType type) {
        return !supports(type);
    }
}
