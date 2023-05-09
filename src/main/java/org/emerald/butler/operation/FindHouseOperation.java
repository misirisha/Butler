package org.emerald.butler.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.emerald.butler.component.Sender;
import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.House;
import org.emerald.butler.entity.TelegramChat;
import org.emerald.butler.repository.DwellerRepository;
import org.emerald.butler.repository.HouseRepository;
import org.emerald.butler.repository.TelegramChatRepository;
import org.emerald.butler.telegram.KeyboardRow;
import org.emerald.butler.telegram.ReplyKeyboardMarkupBuilder;
import org.emerald.butler.util.Format;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@Component
public class FindHouseOperation extends AbstractOperation {
    private String region;
    private String city;
    private String street;
    private String number;
    private final HouseRepository houseRepository;
    private final TelegramChatRepository telegramChatRepository;

    @Autowired
    public FindHouseOperation(UserCommandManager userCommandManager,
                              Sender sender,
                              DwellerRepository dwellerRepository,
                              HouseRepository houseRepository,
                              TelegramChatRepository telegramChatRepository) {
        super(userCommandManager, sender, dwellerRepository);
        this.houseRepository = houseRepository;
        this.telegramChatRepository = telegramChatRepository;
    }

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return Map.of(
                "start", this::onStart,
                "город", this::cityInput,
                "улица", this::streetInput,
                "номер дома", this::numberInput,
                "проверка адреса", this::addressChecking,
                "поиск", this::changeAddress
        );
    }


    private void onStart(Context context) {
        userCommandManager.updateProgress(context.userCommand, "город");
        sender.send("Введите регион \n" +
                " Пример: \n" +
                "  Республика Адыгея \n" +
                "  Московская обсласть\n" +
                "  Краснодарский край\n" +
                "  Еврейская автономная область\n" +
                "  Ненецкий Автономный округ", context.update, startMarkup());
    }

    private ReplyKeyboardMarkup startMarkup() {
        return new ReplyKeyboardMarkupBuilder()
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .withKeyboardWrapped(List.of(
                        new KeyboardRow(new KeyboardButton("Отмена"))
                ))
                .build();
    }

    private void cityInput(Context context) {
        if (context.text.equals("Отмена")){
            onCancel(context);
        } else {
            this.region = context.text;
            userCommandManager.updateProgress(context.userCommand, "улица");
            sender.send("Введите город \n" +
                    " Пример: \n" +
                    "  Москва", context.update, defaultMarkup());
        }
    }

    private void streetInput(Context context) {
        if(context.text.equals("Назад")){
            onBack(context);
            sender.send("Введите регион \n" +
                    " Пример: \n" +
                    "  Республика Адыгея \n" +
                    "  Московская обсласть\n" +
                    "  Краснодарский край\n" +
                    "  Еврейская автономная область\n" +
                    "  Ненецкий Автономный округ", context.update, startMarkup());
        } else if (context.text.equals("Отмена")){
            onCancel(context);
        } else {
            this.city = context.text;
            userCommandManager.updateProgress(context.userCommand, "номер дома");
            sender.send("Введите улицу \n" +
                    " Пример: \n" +
                    "  Тверская", context.update, defaultMarkup());
        }
    }

    private void numberInput(Context context) {
        if(context.text.equals("Назад")){
            onBack(context);
            sender.send("Введите город \n" +
                    " Пример: \n" +
                    "  Москва", context.update, defaultMarkup());
        } else if (context.text.equals("Отмена")){
            onCancel(context);
        } else {
            this.street = context.text;
            userCommandManager.updateProgress(context.userCommand, "проверка адреса");
            sender.send("Введите улицу \n" +
                    " Пример: \n" +
                    "  14" +
                    "  14а" +
                    "  14/8", context.update, defaultMarkup());
        }
    }

    private void addressChecking(Context context) {
        if(context.text.equals("Назад")){
            onBack(context);
            sender.send("Введите улицу \n" +
                    " Пример: \n" +
                    "  Тверская", context.update, defaultMarkup());
        } else if (context.text.equals("Отмена")){
            onCancel(context);
        } else {
            this.number = context.text;
            Optional<House> houseOptional = houseRepository.findByRegionAndCityAndStreetAndNumber(this.region, this.city, this.street, this.number);
            Optional<TelegramChat> chatOptional;
            String result = "";
            if (houseOptional.isEmpty()){
                result = "По адресу: " +
                        new Format("{}, {}, ул. {}, {}", this.region, this.city, this.street, this.number) +
                        "\n" +
                        "чат не найден";

            }else {
                House house = houseOptional.orElseThrow();
                chatOptional = telegramChatRepository.findByHouse(house);

                if(chatOptional.isEmpty()){
                    result = "Ошибка системы";
                }else {
                    CreateChatInviteLink request = new CreateChatInviteLink();
                    request.setChatId(chatOptional.orElseThrow().getTelegramChatId());
                    request.setMemberLimit(1);
                    ChatInviteLink link = sender.execute(request);
                    result = "По адресу: " +
                            new Format("{}, {}, ул. {}, {}", this.region, this.city, this.street, this.number) +
                            "\n" +
                            "был найден чат: \n" +
                            link.getInviteLink();
                }
            }

            userCommandManager.updateProgress(context.userCommand, "поиск");
            sender.send(result, context.update, addressCheckingMarkup());
        }
    }

    private void changeAddress(Context context) {
        if(context.text.equals("Изменить")){
            userCommandManager.updateProgress(context.userCommand, "start");
        }else if(context.text.equals("Отмена")){
           onCancel(context);
        } else{
            onError(context);
        }
    }

    private ReplyKeyboardMarkup addressCheckingMarkup() {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = List.of(
                new KeyboardRow(new KeyboardButton("Изменить")),
                new KeyboardRow(new KeyboardButton("Отмена"))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
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
    public Command getCommand() {
        return Command.FIND_HOUSE;
    }

    @Override
    public boolean supports(Command command) {
        return command == Command.FIND_HOUSE;
    }

    @Override
    public String toString() {
        return "FindHouseOperation";
    }
}
