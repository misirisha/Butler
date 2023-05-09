package org.emerald.butler.operation;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.CommandType;
import org.emerald.butler.operation.invalid.ChannelNotSupportedOperation;
import org.emerald.butler.operation.invalid.GroupNotSupportedIntermediateOperation;
import org.emerald.butler.operation.invalid.GroupNotSupportedOperation;
import org.emerald.butler.operation.invalid.PrivateNotSupportedIntermediateOperation;
import org.emerald.butler.operation.invalid.PrivateNotSupportedOperation;
import org.emerald.butler.operation.invalid.SupergroupNotSupportedIntermediateOperation;
import org.emerald.butler.operation.invalid.SupergroupNotSupportedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class OperationFactory {
    private static final Logger log = LoggerFactory.getLogger(OperationFactory.class);

    private final ApplicationContext context;
    private final UserCommandManager userCommandManager;
    private final List<Operation> operations;

    @Autowired
    public OperationFactory(ApplicationContext context,
                            UserCommandManager userCommandManager,
                            @Lazy List<Operation> operations) {
        this.context = context;
        this.userCommandManager = userCommandManager;
        this.operations = operations;
    }

    public Optional<Command> findCurrentCommand(Update update) {
        if (!update.hasMessage()) {
            return Optional.empty();
        }

        final Message message = update.getMessage();
        final User from = message.getFrom();

        return userCommandManager.findCurrent(from)
                .map(userCommand -> Command.getById(userCommand.getCommand()));
    }


    public Optional<Operation> getOperation(Update update) {
        if (!update.hasMessage()) {
            return Optional.empty();
        }

        Message message = update.getMessage();

        final Optional<Command> optionalCommand = findCurrentCommand(update);
        if (optionalCommand.isPresent()) {
            final Command command = optionalCommand.get();
            if (chatTypeIsWrong(command, message)) {
                return Optional.of(getWrongChatTypeIntermediateOperation(command, message));
            }
        }

        if (optionalCommand.isPresent()) {
            return pickupOperation(optionalCommand.get());
        }
        return Optional.empty();
    }


    public Optional<Operation> beginOperation(Update update) {
        if (!update.hasMessage()) {
            return Optional.empty();
        }
        final Message message = update.getMessage();
        final Optional<Command> commandOptional = Command.findByTelegramCommand(asTelegramCommand(message));
        if (commandOptional.isEmpty()) {
            return Optional.empty();
        }

        final Command command = commandOptional.get();
        if (chatTypeIsWrong(command, message)) {
            return Optional.of(getWrongChatTypeOperation(command, message));
        }

        userCommandManager.set(message.getFrom(), command, "start");

        return pickupOperation(command);
    }

    private Optional<Operation> pickupOperation(Command command) {
        for (Operation operation : operations) {
            if (operation.supports(command)) {
                return Optional.of(operation);
            }
        }

        return Optional.empty();
    }

    private String asTelegramCommand(Message message) {
        final String text = message.getText();
        if (!text.contains("@")) {
            return text;
        }

        return text.substring(0, text.indexOf('@'));
    }

    private boolean chatTypeIsWrong(Command command, Message message) {
        return command.doesNotSupport(getType(message));
    }

    private Operation getWrongChatTypeIntermediateOperation(Command command, Message message) {
        Map<CommandType, Supplier<Operation>> map = Map.of(
                CommandType.CHANNEL, () -> context.getBean(ChannelNotSupportedOperation.class),
                CommandType.PRIVATE, () -> context.getBean(PrivateNotSupportedIntermediateOperation.class),
                CommandType.GROUP, () -> context.getBean(GroupNotSupportedIntermediateOperation.class),
                CommandType.SUPERGROUP, () -> context.getBean(SupergroupNotSupportedIntermediateOperation.class)
        );

        CommandType type = getType(message);
        if (command.doesNotSupport(type)) {
            return map.get(type).get();
        }

        throw new IllegalArgumentException("Given operation is valid");
    }

    private Operation getWrongChatTypeOperation(Command command, Message message) {
        Map<CommandType, Supplier<Operation>> map = Map.of(
                CommandType.CHANNEL, () -> context.getBean(ChannelNotSupportedOperation.class),
                CommandType.PRIVATE, () -> context.getBean(PrivateNotSupportedOperation.class),
                CommandType.GROUP, () -> context.getBean(GroupNotSupportedOperation.class),
                CommandType.SUPERGROUP, () -> context.getBean(SupergroupNotSupportedOperation.class)
        );

        CommandType type = getType(message);
        if (command.doesNotSupport(type)) {
            return map.get(type).get();
        }

        throw new IllegalArgumentException("Given operation is valid");
    }

    private CommandType getType(Message message) {
        final String chatType = message.getChat().getType();
        return CommandType.getByCode(chatType);
    }
}
