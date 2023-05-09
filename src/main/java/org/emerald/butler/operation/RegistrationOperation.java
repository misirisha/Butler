package org.emerald.butler.operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.jmix.core.Metadata;
import org.emerald.butler.component.Sender;
import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.Transport;
import org.emerald.butler.repository.DwellerRepository;
import org.emerald.butler.repository.TransportJmixRepository;
import org.emerald.butler.service.TransportService;
import org.emerald.butler.telegram.KeyboardRow;
import org.emerald.butler.telegram.ReplyKeyboardMarkupBuilder;
import org.emerald.butler.util.Format;
import org.emerald.butler.util.NumericCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@Component
public class RegistrationOperation extends AbstractOperation {
    private final Metadata metadata;
    private final TransportService transportService;
    private final TransportJmixRepository transportJmixRepository;

    @Autowired
    public RegistrationOperation(UserCommandManager userCommandManager,
                                 Sender sender,
                                 DwellerRepository dwellerRepository,
                                 TransportService transportService,
                                 TransportJmixRepository transportJmixRepository,
                                 Metadata metadata) {
        super(userCommandManager, sender, dwellerRepository);
        this.transportService = transportService;
        this.transportJmixRepository = transportJmixRepository;
        this.metadata = metadata;
    }

    @Override
    public Command getCommand() {
        return Command.ABOUT;
    }

    @Override
    public boolean supports(Command command) {
        return command == Command.ABOUT;
    }

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return Map.of(
                "start", this::onStart,
                "обо мне", this::infoAboutMe,
                "управление транспортом", this::onTransportManagement,
                "добавить транспорт", this::onAddTransport,
                "внутри списка транспорта", this::updateListTransports,
                "удалить транспорт", this::onDeleteTransport
        );
    }

    private void onStart(Context context) {
        userCommandManager.updateProgress(context.userCommand, "обо мне");
        sender.send("Выберите следующее действие", context.update, startMarkup());
    }

    private ReplyKeyboardMarkup startMarkup() {
        return new ReplyKeyboardMarkupBuilder()
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .keyboard(List.of(
                        new KeyboardRow(new KeyboardButton("Автомобили")),
                        new KeyboardRow(new KeyboardButton("Отмена"))
                ))
                .build();
    }

    private void infoAboutMe(Context context) {
        if (context.text.equals("Автомобили")) {
            userCommandManager.updateProgress(context.userCommand, "управление транспортом");
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals("Отмена")){
            onCancel(context);
        } else{
            onError(context);
        }
    }

    private ReplyKeyboardMarkup transportManagementMarkup() {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = List.of(
                new KeyboardRow(new KeyboardButton("Список")),
                new KeyboardRow(new KeyboardButton("Добавить")),
                new KeyboardRow(new KeyboardButton("Назад")),
                new KeyboardRow(new KeyboardButton("Отмена"))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }

    private void onTransportManagement(Context context) {
        if (context.text.equals("Список")) {
            List<Transport> transports = transportJmixRepository.findAllByDwellerTelegramId(context.user.getId());
            if (transports.isEmpty()){
                sender.send("Список пуст", context.update, transportManagementMarkup());
                return;
            }

            StringBuilder messageBuilder = new StringBuilder();
            for (Transport transport : transports) {
                messageBuilder.append(new Format("{}. {}", transport.getOrder(), transport.getNumber()))
                        .append(System.lineSeparator());
            }

            userCommandManager.updateProgress(context.userCommand, "внутри списка транспорта");
            sender.send(messageBuilder.toString(), context.update, updateListTransportsMarkup());
        } else if (context.text.equals("Добавить")) {
            userCommandManager.updateProgress(context.userCommand, "добавить транспорт");
            sender.send("Введите номер машины", context.update, defaultMarkup());
        } else if (context.text.equals("Назад")) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, startMarkup());
        } else if (context.text.equals("Отмена")) {
            onCancel(context);
        } else {
            onError(context);
        }
    }

    //TODO: проверка соответсвия вводимого номера машины ГОСТу
    private void onAddTransport(Context context) {
        if (context.text.equals("Назад")) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals("Отмена")) {
            onCancel(context);
        } else {
            Optional<Dweller> dwellerOptional = dwellerRepository.findByTelegramId(context.user.getId());
            Dweller dweller = dwellerOptional.orElseThrow();
            long previousOrder = transportJmixRepository.findFirstByDwellerOrderByOrderDesc(dweller)
                    .map(Transport::getOrder)
                    .orElse(0L);
            Transport transport = Transport.builder(metadata)
                    .order(previousOrder + 1)
                    .dweller(dweller)
                    .number(context.text)
                    .build();
            transportJmixRepository.save(transport);

            onBack(context);
            ReplyKeyboardMarkup returnedStageMarkup;
            if (context.userCommand.getProgress().equals("внутри списка транспорта")) {
                returnedStageMarkup = updateListTransportsMarkup();
            } else {
                returnedStageMarkup = transportManagementMarkup();
            }
            sender.send("Номер добавлен", context.update, returnedStageMarkup);
        }
    }

    private void updateListTransports(Context context) {
        if (context.text.equals("Удалить")) {
            userCommandManager.updateProgress(context.userCommand, "удалить транспорт");
            sender.send("Введите порядковый номер или номер машины", context.update, updateListTransportsMarkup());
        } else if (context.text.equals("Добавить")) {
            userCommandManager.updateProgress(context.userCommand, "добавить транспорт");
            sender.send("Введите номер машины", context.update, defaultMarkup());
        } else if (context.text.equals("Назад")) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals("Отмена")) {
            onCancel(context);
        } else {
            onError(context);
        }
    }

    private ReplyKeyboardMarkup updateListTransportsMarkup() {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = List.of(
                new KeyboardRow(new KeyboardButton("Удалить")),
                new KeyboardRow(new KeyboardButton("Добавить")),
                new KeyboardRow(new KeyboardButton("Назад")),
                new KeyboardRow(new KeyboardButton("Отмена"))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }

    private void onDeleteTransport(Context context) {
        if (context.text.equals("Назад")) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals("Отмена")) {
            onCancel(context);
        } else {
            Optional<Dweller> dwellerOptional = dwellerRepository.findByTelegramId(context.user.getId());
            Dweller dweller = dwellerOptional.orElseThrow();
            boolean isOrderEntered = new NumericCheck(context.text).isLong();
            if (isOrderEntered) {
                long order = Long.parseLong(context.text);
                transportJmixRepository.findByDwellerAndOrder(dweller, order).ifPresentOrElse(
                        transport -> {
                            transportService.delete(transport);
                            onBack(context, 2);
                            sender.send("Транспорт успешно удален", context.update, transportManagementMarkup());
                        }, () -> {
                            sender.send(
                                    "Транспорт с переданным порядковым номером не найден. Передано: " + order,
                                    context.update,
                                    updateListTransportsMarkup()
                            );
                            onBack(context);
                        }
                );
            } else {
                String number = context.text;
                final Collection<Transport> transports = transportJmixRepository.findAllByDwellerAndNumber(
                        dweller, number
                );
                if (transports.isEmpty()) {
                    sender.send(
                            "Транспорт с переданным номером не найден. Передано: " + number,
                            context.update,
                            updateListTransportsMarkup()
                    );
                    onBack(context);
                } else {
                    transportService.delete(transports);
                    onBack(context, 2);
                    sender.send("Транспорт успешно удален", context.update, transportManagementMarkup());
                }
            }
        }
    }

    private ReplyKeyboardMarkup defaultMarkup() {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = List.of(
                new KeyboardRow(new KeyboardButton("Назад")),
                new KeyboardRow(new KeyboardButton("Отмена"))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }


    @Override
    public String toString() {
        return "RegistrationOperation";
    }
}
