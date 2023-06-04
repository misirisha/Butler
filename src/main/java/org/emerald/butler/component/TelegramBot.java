package org.emerald.butler.component;

import java.util.Optional;
import java.util.UUID;

import io.jmix.core.Metadata;
import io.jmix.core.security.SystemAuthenticator;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.config.BotConfig;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.ApartmentRole;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.emerald.butler.operation.Operation;
import org.emerald.butler.operation.OperationFactory;
import org.emerald.butler.repository.ApartmentRepository;
import org.emerald.butler.repository.DwellerApartmentRoleRepository;
import org.emerald.butler.repository.DwellerRepository;
import org.emerald.butler.util.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
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
    private final DwellerApartmentRoleRepository dwellerApartmentRoleRepository;
    private final ApartmentRepository apartmentRepository;
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
        authenticator.runWithSystem(() -> {
            if (update.hasCallbackQuery()) {
                updateWithCallBack(update);
            } else {
                updateWithMessage(update);
            }
        });
    }

    private void updateWithCallBack(Update update) {
        CallbackQuery query = update.getCallbackQuery();
        //a_t_a = add_to_apartment
        //a = approve
        //r = rejected
        if (query.getData().startsWith("a_t_a")) {
            String[] parts = query.getData().replaceAll("a_t_a", "").split(" ");
            String result = parts[0].trim();
            String apartmentId = parts[1].trim();
            String dwellerId = parts[2].trim();
            Optional<Dweller> dwellerOptional = dwellerRepository.findByTelegramId(Long.parseLong(dwellerId));
            Optional<Apartment> apartmentOptional = apartmentRepository.findById(UUID.fromString(apartmentId));
            if (dwellerOptional.isEmpty()){
                return;
            }
            if (apartmentOptional.isEmpty()){
                return;
            }

            if(result.equals("a")){
                DwellerApartmentRole notSaved = DwellerApartmentRole.builder(metadata)
                        .dweller(dwellerOptional.orElseThrow())
                        .apartment(apartmentOptional.orElseThrow())
                        .apartmentRole(ApartmentRole.DWELLER)
                        .build();
                dwellerApartmentRoleRepository.save(notSaved);
                //TODO: Сделать более понятное сообщение. Добавить адрес квартиры, в которую добавлен пользователь
                sendToOtherChat("Заявка на добавление одобрена", dwellerId);
            } else if(result.equals("reject")){
                sendToOtherChat("Заявка на добавление не одобрена", dwellerId);
            }
        }
    }

    private void updateWithMessage(Update update) {
        if (!update.hasMessage()) {
            return;
        }
        if (!update.getMessage().hasText()) {
            return;
        }

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
