package org.emerald.butler.operation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.Constant;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.Transport;
import org.emerald.butler.repository.TransportJmixRepository;
import org.emerald.butler.service.DwellerService;
import org.emerald.butler.service.TransportService;
import org.emerald.butler.telegram.KeyboardRow;
import org.emerald.butler.telegram.ReplyKeyboardMarkupBuilder;
import org.emerald.butler.util.Format;
import org.emerald.butler.util.NumericCheck;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@RequiredArgsConstructor
@Component
public class RegistrationOperation extends AbstractOperation {
    private final Map<Supplier<? extends ReplyKeyboard>, Consumer<Context>> MARKUP_TO_HANDLER =
            new ImmutableMap.Builder<Supplier<? extends ReplyKeyboard>, Consumer<Context>>()
                    .put(this::startMarkup, this::infoAboutMe)
                    .put(this::transportManagementMarkup, this::onTransportManagement)
                    .put(this::dataMenuMarkup, this::onDataMenu)
                    .put(this::birthDateMenuMarkup, this::onBirthDateMenu)
                    .put(this::metersDataMarkup, this::onMetersData)
                    .build();

    private final Metadata metadata;
    private final TransportService transportService;
    private final TransportJmixRepository transportJmixRepository;
    private final DwellerService dwellerService;

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
        return new ImmutableMap.Builder<String, Consumer<Context>>()
                .put("start", this::onStart)
                .put("обо мне", this::infoAboutMe)
                .put("управление транспортом", this::onTransportManagement)
                .put("добавить транспорт", this::onAddTransport)
                .put("внутри списка транспорта", this::updateListTransports)
                .put("удалить транспорт", this::onDeleteTransport)
                .put("меню данных", this::onDataMenu)
                .put("меню даты рождения", this::onBirthDateMenu)
                .put("ввод даты рождения", this::onSetupBirthDate)
                .put("меню показаний счетчиков", this::onMetersData)
                .put("выбор квартиры для показаний счетчиков", this::onApartmentPickup)
                .build();
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
                        new KeyboardRow(new KeyboardButton("Данные")),
                        new KeyboardRow(new KeyboardButton(Constant.CANCEL))
                ))
                .build();
    }

    private void infoAboutMe(Context context) {
        if (context.text.equals("Автомобили")) {
            userCommandManager.updateProgress(context.userCommand, "управление транспортом");
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals("Данные")) {
            userCommandManager.updateProgress(context.userCommand, "меню данных");
            sender.send("Выберите следующее действие", context.update, dataMenuMarkup());
        } else if (context.text.equals(Constant.CANCEL)){
            onCancel(context);
        } else {
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
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
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
        } else if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, startMarkup());
        } else if (context.text.equals(Constant.CANCEL)) {
            onCancel(context);
        } else {
            onError(context);
        }
    }

    //TODO: проверка соответсвия вводимого номера машины ГОСТу
    private void onAddTransport(Context context) {
        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals(Constant.CANCEL)) {
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
        } else if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals(Constant.CANCEL)) {
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
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }

    private void onDeleteTransport(Context context) {
        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, transportManagementMarkup());
        } else if (context.text.equals(Constant.CANCEL)) {
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
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }

    private ReplyKeyboardMarkup dataMenuMarkup() {
        return markup(List.of(
                new KeyboardRow(new KeyboardButton("Дата рождения")),
                //new KeyboardRow(new KeyboardButton("Показания счетчиков")),
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        ));
    }

    private void onDataMenu(Context context) {
        if (defaultHandle(context).isHandled()) {
            return;
        }

        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, startMarkup());
        } else if (context.text.equals("Дата рождения")) {
            final String message = dwellerService.findBirthDate(context)
                    .map(birthDate -> "Указанная дата рождения: " + birthDate)
                    .orElse("Ваша дата рождения не указана");

            userCommandManager.updateProgress(context.userCommand, "меню даты рождения");
            sender.send(message, context.update, birthDateMenuMarkup());
        } else if (context.text.equals("Показания счетчиков")) {
            userCommandManager.updateProgress(context.userCommand, "выбор квартиры для показаний счетчиков");
            sender.send("Выберите квартиру", context.update, pickupApartmentMarkup(context));
        } else {
            onError(context);
        }
    }

    private ReplyKeyboardMarkup pickupApartmentMarkup(Context context) {
        final Collection<Integer> apartments = dwellerService.getApartmentsNumbers(context);
        final List<KeyboardRow> rows = new ArrayList<>();
        for (Integer apartment : apartments) {
            rows.add(new KeyboardRow(new KeyboardButton(apartment.toString())));
        }
        rows.add(new KeyboardRow(new KeyboardButton(Constant.BACK)));
        rows.add(new KeyboardRow(new KeyboardButton(Constant.CANCEL)));
        return markup(rows);
    }

    private ReplyKeyboardMarkup birthDateMenuMarkup() {
        return markup(List.of(
                new KeyboardRow(new KeyboardButton("Указать")),
                new KeyboardRow(new KeyboardButton("Удалить")),
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        ));
    }

    private void onBirthDateMenu(Context context) {
        if (defaultHandle(context).isHandled()) {
            return;
        }

        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, dataMenuMarkup());
        } else if (context.text.equals("Указать")) {
            userCommandManager.updateProgress(context.userCommand, "ввод даты рождения");
            sender.send("Введите вашу дату рождения. Пример: 25.08.2022, либо 25 08 2022", context.update, defaultMarkup());
        } else if (context.text.equals("Удалить")) {
            dwellerService.clearBirthDate(context);
            sender.send("Дата рождения удалена", context.update, birthDateMenuMarkup());
        } else {
            onError(context);
        }
    }

    private void onSetupBirthDate(Context context) {
        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, birthDateMenuMarkup());
        } else if (context.text.equals(Constant.CANCEL)) {
            onCancel(context);
        } else {
            final Optional<LocalDate> optionalBirthDate = dwellerService.parse(context.text);
            if (optionalBirthDate.isEmpty()) {
                sender.send("Формат некорректен. Попробуйте ещё раз", context.update, defaultMarkup());
                return;
            }

            dwellerService.updateBirthDate(context, optionalBirthDate.get());
            onBack(context);
            sender.send("Дата рождения успешно сохранена", context.update, birthDateMenuMarkup());
        }
    }

    private void onApartmentPickup(Context context) {
        if (defaultHandle(context).isHandled()) {
            return;
        }

        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, dataMenuMarkup());
        } else {
            final NumericCheck check = new NumericCheck(context.text);
            if (!check.isInteger()) {
                sender.send("Некорректный формат номера квартиры", context.update, pickupApartmentMarkup(context));
                return;
            }

            final Integer apartmentNumber = Integer.parseInt(context.text);

        }
    }

    private ReplyKeyboardMarkup metersDataMarkup() {
        return markup(List.of(
                new KeyboardRow(new KeyboardButton("Просмотреть")),
                new KeyboardRow(new KeyboardButton("Записать")),
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        ));
    }

    private void onMetersData(Context context) {
        if (defaultHandle(context).isHandled()) {
            return;
        }

        //сначала выбрать квартиру для показаний
        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.send("Выберите следующее действие", context.update, dataMenuMarkup());
        } else if (context.text.equals("Просмотреть")) {
            //может быть много квартир. Надо учитывать. Здесь выбираем квартиру из списка
            //TODO: show latest 5 meters [data -- water -- cold-water -- gaz -- electro]
        } else if (context.text.equals("Записать")) {

            //TODO: write in specific format
        } else {
            onError(context);
        }
    }

    private ReplyKeyboardMarkup markup(List<? extends KeyboardRow> rows) {
        return new ReplyKeyboardMarkupBuilder()
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .keyboard(rows)
                .build();
    }
}
