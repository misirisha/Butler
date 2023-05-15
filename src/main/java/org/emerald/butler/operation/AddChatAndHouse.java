package org.emerald.butler.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.jmix.core.Metadata;
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
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@Component
public class AddChatAndHouse extends AbstractOperation {
    private String region;
    private String city;
    private String street;
    private String number;
    private final Metadata metadata;
    private final HouseRepository houseRepository;
    private final TelegramChatRepository telegramChatRepository;

    public AddChatAndHouse(UserCommandManager userCommandManager,
                           Sender sender,
                           DwellerRepository dwellerRepository,
                           HouseRepository houseRepository,
                           TelegramChatRepository telegramChatRepository,
                           Metadata metadata) {
        super(userCommandManager, sender, dwellerRepository);
        this.houseRepository = houseRepository;
        this.telegramChatRepository = telegramChatRepository;
        this.metadata = metadata;
    }

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return Map.of(
                "start", this::onStart,
                "город", this::cityInput,
                "улица", this::streetInput,
                "номер дома", this::numberInput,
                "добавление дома", this::addingHouse,
                "да или нет", this::yesOrNo
        );
    }


//TODO: проверить не привязан ли чат уже к какому-то дому

    private void onStart(Context context) {
        userCommandManager.updateProgress(context.userCommand, "город");
        sender.sendToChat("Введите регион \n" +
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
            sender.sendToChat("Введите город \n" +
                    " Пример: \n" +
                    "  Москва", context.update, defaultMarkup());
        }
    }

    private void streetInput(Context context) {
        if(context.text.equals("Назад")){
            onBack(context);
            sender.sendToChat("Введите регион \n" +
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
            sender.sendToChat("Введите улицу \n" +
                    " Пример: \n" +
                    "  Тверская", context.update, defaultMarkup());
        }
    }

    private void numberInput(Context context) {
        if(context.text.equals("Назад")){
            onBack(context);
            sender.sendToChat("Введите город \n" +
                    " Пример: \n" +
                    "  Москва", context.update, defaultMarkup());
        } else if (context.text.equals("Отмена")){
            onCancel(context);
        } else {
            this.street = context.text;
            userCommandManager.updateProgress(context.userCommand, "добавление дома");
            sender.sendToChat("Введите улицу \n" +
                    " Пример: \n" +
                    "  14" +
                    "  14а" +
                    "  14/8", context.update, defaultMarkup());
        }
    }

    private void addingHouse(Context context) {
        if(context.text.equals("Назад")){
            onBack(context);
            sender.sendToChat("Введите улицу \n" +
                    " Пример: \n" +
                    "  Тверская", context.update, defaultMarkup());
        } else if (context.text.equals("Отмена")){
            onCancel(context);
        } else {
            this.number = context.text;
            String result = "Привязать чат к дому: " +
                    new Format("{}, {}, ул. {}, {}", this.region, this.city, this.street, this.number) + "?";
            userCommandManager.updateProgress(context.userCommand, "да или нет");
            sender.sendToChat(result, context.update, yesOrNoMarkup());
        }

    }

    private void yesOrNo(Context context){
        if(context.text.equals("Да")){
            House notSaved = House.builder(metadata)
                    .region(this.region)
                    .city(this.city)
                    .street(this.street)
                    .number(this.number)
                    .build();
            House house = houseRepository.save(notSaved);

            TelegramChat telegramChat = TelegramChat.builder(metadata)
                    .telegramChatId(context.update.getMessage().getChatId().toString())
                    .house(house)
                    .build();
            telegramChatRepository.save(telegramChat);

            userCommandManager.clear(context.user);
            sender.sendToChat("Успешно", context.update);

        } else if (context.text.equals("Нет")){
            onCancel(context);
        } else {
            onError(context);
        }
    }

    private ReplyKeyboardMarkup yesOrNoMarkup() {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = List.of(
                new KeyboardRow(new KeyboardButton("Да")),
                new KeyboardRow(new KeyboardButton("Нет"))
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
        return Command.ADD_CHAT_AND_HOUSE;
    }

    @Override
    public boolean supports(Command command) {
        return command == Command.ADD_CHAT_AND_HOUSE;
    }

}
