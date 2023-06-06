package org.emerald.butler.operation;

import io.jmix.core.Metadata;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.ApartmentRole;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.Dweller;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.emerald.butler.entity.DwellerChatRole;
import org.emerald.butler.entity.House;
import org.emerald.butler.entity.TelegramChat;
import org.emerald.butler.repository.ApartmentRepository;
import org.emerald.butler.repository.DwellerApartmentRoleRepository;
import org.emerald.butler.repository.DwellerChatRoleRepository;
import org.emerald.butler.repository.HouseRepository;
import org.emerald.butler.repository.TelegramChatRepository;
import org.emerald.butler.telegram.KeyboardRow;
import org.emerald.butler.telegram.TelegramUtils;
import org.emerald.butler.util.Format;
import org.emerald.butler.util.NumericCheck;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Component
public class AddApartment extends AbstractOperation {
    private final HouseRepository houseRepository;
    private final TelegramChatRepository telegramChatRepository;
    private final DwellerChatRoleRepository dwellerChatRoleRepository;
    private final DwellerApartmentRoleRepository dwellerApartmentRoleRepository;
    private final ApartmentRepository apartmentRepository;
    private final Metadata metadata;

    private House house;
    private Apartment apartment;
    private Integer frontDoor;
    private Integer floor;
    private Integer number;

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return Map.of(
                "start", this::onStart,
                "подъезд", this::frontDoorInput,
                "выбор дома", this::houseSelection,
                "этаж", this::floorInput,
                "квартира", this::numberInput,
                "да или нет", this::yesOrNo
        );
    }


    private void onStart(Context context) {
        if (context.update.getMessage().getChat().getType().equals("private")){
            List<DwellerChatRole> dwellerChatRoleList =
            dwellerChatRoleRepository.findAllByDwellerTelegramId(context.update.getMessage().getFrom().getId());
            if(dwellerChatRoleList.isEmpty()){
                userCommandManager.clear(context.user);
                sender.send("Вы не зарегистрированы ни в одном доме", context.update);
                return;
            }
            List<String> houses = new ArrayList<>();
            for (DwellerChatRole dweller: dwellerChatRoleList){
                houses.add(dweller.getChat().getHouse().toString());
            }
            userCommandManager.updateProgress(context.userCommand, "выбор дома");
            sender.send("Выберете дом", context.update, houseSelectionMarkup(houses));

        } else if (context.update.getMessage().getChat().getType().equals("group")
                ||context.update.getMessage().getChat().getType().equals("supergroup")) {

            Optional<TelegramChat> telegramChatOptional =
                    telegramChatRepository.findByTelegramChatId(context.update.getMessage().getChatId().toString());
            if(telegramChatOptional.isEmpty()){
                userCommandManager.clear(context.user);
                sender.send("Невозможно добавить квартиру \n Пожалуйста, привяжите группу к дому. Команда: addhouse", context.update);
                return;
            }
            house = telegramChatOptional.orElseThrow().getHouse();
            if(Objects.isNull(house)){
                onError(context);
                return;
            }
            userCommandManager.updateProgress(context.userCommand, "подъезд");
            sender.send("Введите номер подъезда", context.update, startMarkup());
        }
    }

    private void houseSelection(Context context){
        if(context.text.equals("Отмена")){
            onCancel(context);
        } else{
            String[] address = context.update.getMessage().getText().split(",");
            if(address.length == 4){
                for (int i = 0; i<address.length; i++){
                    address[i] = address[i].trim();
                }
                Optional<House> houseOptional = houseRepository
                        .findByRegionAndCityAndStreetAndNumber(address[0], address[1],address[2],address[3]);
                if(houseOptional.isPresent()){
                    house = houseOptional.get();
                    userCommandManager.updateProgress(context.userCommand, "подъезд");
                    sender.send("Введите номер подъезда", context.update, startMarkup());
                }else{
                    sender.send("Дом не найден", context.update);
                }

            }else{
                onError(context);
            }
        }
    }

    private void frontDoorInput(Context context){
        if(context.text.equals("Отмена")){
            onCancel(context);
        }else {
            String raw = context.update.getMessage().getText();
            if (new NumericCheck(raw).isInteger()) {
                frontDoor = Integer.parseInt(context.update.getMessage().getText());
                userCommandManager.updateProgress(context.userCommand, "этаж");
                sender.send("Введите этаж", context.update, defaultMarkup());
            }else {
                sender.send("Некорректный ввод. \nВведите номер подъезда", context.update, startMarkup());
            }
        }
    }

    private void floorInput(Context context){
        if(context.text.equals("Отмена")){
            onCancel(context);
        }else if(context.text.equals("Назад")) {
            onBack(context);
            sender.send("Введите номер подъезда", context.update, startMarkup());
        }else {
            String raw = context.update.getMessage().getText();
            if (new NumericCheck(raw).isInteger()) {
                floor = Integer.parseInt(context.update.getMessage().getText());
                userCommandManager.updateProgress(context.userCommand, "квартира");
                sender.send("Введите номер квартиры", context.update, defaultMarkup());
            }else {
                sender.send("Некорректный ввод. \nВведите этаж", context.update, defaultMarkup());
            }
        }
    }

    private void numberInput(Context context){
        if(context.text.equals("Отмена")){
            onCancel(context);
        }else if(context.text.equals("Назад")) {
            onBack(context);
            sender.send("Введите этаж", context.update, defaultMarkup());
        }else {
            String raw = context.update.getMessage().getText();
            if (new NumericCheck(raw).isInteger()) {
                number = Integer.parseInt(context.update.getMessage().getText());
                Optional<Apartment> apartmentOptional =
                        apartmentRepository.findByHouseIdAndFrontDoorAndFloorAndNumber(house.getId(), frontDoor, floor, number);

                if(apartmentOptional.isEmpty()){
                    Apartment notSavedApartment = Apartment.builder(metadata)
                            .house(house)
                            .frontDoor(frontDoor)
                            .floor(floor)
                            .number(number)
                            .build();
                    Apartment savedApartment = apartmentRepository.save(notSavedApartment);

                    DwellerApartmentRole notSavedRole = DwellerApartmentRole.builder(metadata)
                            .apartment(savedApartment)
                            .dweller(dwellerRepository.findByTelegramId(context.user.getId()).orElseThrow())
                            .apartmentRole(ApartmentRole.OWNER)
                            .build();
                    dwellerApartmentRoleRepository.save(notSavedRole);

                    userCommandManager.clear(context.user);
                    sender.send("Квартира сохранена", context.update);

                }else {
                    apartment = apartmentOptional.orElseThrow();
                    userCommandManager.updateProgress(context.userCommand, "да или нет");
                    sender.send("У этой квартиры уже есть владелец. Отправить ему заявку на добавление в качестве жителя?",
                            context.update, yesOrNoMarkup());
                }
            }else {
                sender.send("Некорректный ввод. \nВведите номер квартиры", context.update, defaultMarkup());
            }
        }
    }

    private void yesOrNo(Context context) {
        if (context.text.equals("Да")) {
            Optional<DwellerApartmentRole> dwellerApartmentRoleOptional =
                    dwellerApartmentRoleRepository.findByApartmentIdAndApartmentRole(apartment.getId(), ApartmentRole.OWNER.getId());
            if (dwellerApartmentRoleOptional.isEmpty()) {
                userCommandManager.clear(context.user);
                onError(context);
                return;
            }
            Dweller owner = dwellerApartmentRoleOptional.get().getDweller();
            userCommandManager.clear(context.user);
            sender.sendToOtherChat(new Format("Пользователь {} хочет добавиться в квартиру {}, кв.{}", 
                    context.user.getUserName(), 
                    house.toString(), 
                    apartment.getNumber()),
                    owner.getTelegramId(),
                    messageForOwnerMarkup(apartment.getId(),context.user.getId()));

        } else if(context.text.equals("Нет")) {
            userCommandManager.clear(context.user);
            onCancel(context);
        } else {
            onError(context);
        }
    }

    private InlineKeyboardMarkup messageForOwnerMarkup(UUID apartmentId, Long dwellerTelegramId) {
        Format approveFormat = new Format("a_t_a a {} {} ",
                apartmentId.toString(),
                dwellerTelegramId.toString());
        Format rejectFormat = new Format("a_t_a r {} {}",
                apartmentId.toString(),
                dwellerTelegramId.toString());

        return InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(TelegramUtils.createInlineButton("Добавить", approveFormat)))
                .keyboardRow(List.of(TelegramUtils.createInlineButton("Не добавлять", rejectFormat)))
                .build();
    }


    private ReplyKeyboardMarkup startMarkup() {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = List.of(
                new KeyboardRow(new KeyboardButton("Отмена"))
        );
        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
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

    private ReplyKeyboardMarkup houseSelectionMarkup(List<String> houses) {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = new ArrayList<>();

        for (String house : houses) {
            rows.add(new KeyboardRow(new KeyboardButton(house)));
        }
        rows.add(new KeyboardRow(new KeyboardButton("Отмена")));

        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }

    @Override
    public Command getCommand() {
        return Command.ADD_APARTMENT;
    }

    @Override
    public boolean supports(Command command) {
        return command == Command.ADD_APARTMENT;
    }
}
