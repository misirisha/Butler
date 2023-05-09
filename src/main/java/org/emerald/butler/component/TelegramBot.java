package org.emerald.butler.component;

import java.util.Optional;

import io.jmix.core.Metadata;
import io.jmix.core.security.SystemAuthenticator;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.config.BotConfig;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.operation.Operation;
import org.emerald.butler.operation.OperationFactory;
import org.emerald.butler.repository.DwellerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

@RequiredArgsConstructor
@Component
public class TelegramBot extends Sender {
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    private final Metadata metadata;
    private final SystemAuthenticator authenticator;
    private final BotConfig botConfig;
    private final DwellerRepository dwellerRepository;
    private final OperationFactory operationFactory;

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        if (!update.getMessage().hasText()) {
            return;
        }

        authenticator.runWithSystem(() -> {
            long chatId = update.getMessage().getChatId();
            User from = update.getMessage().getFrom();
            Optional<Dweller> dwellerOptional = dwellerRepository.findByTelegramId(from.getId());
            if (dwellerOptional.isEmpty()) {
                Dweller notSaved = Dweller.builder(metadata)
                        .telegramId(from.getId())
                        .firstName(from.getFirstName())
                        .lastName(from.getLastName())
                        .userName(from.getUserName())
                        .build();

                dwellerRepository.save(notSaved);
                log.debug("Created dweller for a user. User' username: {}", from.getUserName());
            }

            operationFactory.getOperation(update).ifPresentOrElse(
                    existingOperation -> existingOperation.doOperation(update),
                    () -> beginOperation(update)
            );
        });
    }

    private void beginOperation(Update update) {
        Optional<Operation> operationOptional = operationFactory.beginOperation(update);
        if (operationOptional.isPresent()) {
            operationOptional.get().doOperation(update);
        } else {
            send("Неизвестная команда", update);
        }
    }
}
