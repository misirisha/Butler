package org.emerald.butler.operation;

import org.emerald.butler.component.Sender;
import org.emerald.butler.component.UserCommandManager;
import org.emerald.butler.entity.Apartment;
import org.emerald.butler.entity.ApartmentRole;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.DwellerApartmentRole;
import org.emerald.butler.entity.DwellerChatRole;
import org.emerald.butler.entity.House;
import org.emerald.butler.repository.ApartmentRepository;
import org.emerald.butler.repository.DwellerApartmentRoleRepository;
import org.emerald.butler.repository.DwellerChatRoleRepository;
import org.emerald.butler.repository.DwellerRepository;
import org.emerald.butler.repository.HouseRepository;
import org.emerald.butler.repository.TelegramChatRepository;
import org.emerald.butler.repository.TransportRepository;
import org.emerald.butler.telegram.KeyboardRow;
import org.emerald.butler.telegram.ReplyKeyboardMarkupBuilder;
import org.emerald.butler.util.NumericCheck;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class FindDweller extends AbstractOperation {
    private final TelegramChatRepository telegramChatRepository;
    private final HouseRepository houseRepository;
    private final ApartmentRepository apartmentRepository;
    private final TransportRepository transportRepository;
    private final DwellerChatRoleRepository dwellerChatRoleRepository;
    private final DwellerApartmentRoleRepository dwellerApartmentRoleRepository;
    private House house;
    private Integer number;

    public FindDweller(UserCommandManager userCommandManager,
                       Sender sender,
                       DwellerRepository dwellerRepository,
                       TelegramChatRepository telegramChatRepository,
                       HouseRepository houseRepository,
                       ApartmentRepository apartmentRepository,
                       TransportRepository transportRepository,
                       DwellerChatRoleRepository dwellerChatRoleRepository,
                       DwellerApartmentRoleRepository dwellerApartmentRoleRepository) {
        super(userCommandManager, sender, dwellerRepository);
        this.telegramChatRepository = telegramChatRepository;
        this.houseRepository = houseRepository;
        this.apartmentRepository = apartmentRepository;
        this.transportRepository = transportRepository;
        this.dwellerChatRoleRepository =dwellerChatRoleRepository;
        this.dwellerApartmentRoleRepository = dwellerApartmentRoleRepository;
    }

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return Map.of(
                "start", this::onStart,
                "выбор поиска", this::searchSelection,
                "поиск по номеру квартиры", this::findByApartment,
                "поиск по номеру машины", this::findByTransport,
                "выбор дома", this::houseSelection,
                "квартира", this::numberInput
        );
    }

    private void onStart(Context context) {
        userCommandManager.updateProgress(context.userCommand, "выбор поиска");
        sender.send("Выберите следующее действие", context.update, startMarkup());
    }

    private ReplyKeyboardMarkup startMarkup() {
        return new ReplyKeyboardMarkupBuilder()
                .selective(true)
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .withKeyboardWrapped(List.of(
                        new KeyboardRow(new KeyboardButton("По номеру квартиры")),
                        new KeyboardRow(new KeyboardButton("По номеру машины")),
                        new KeyboardRow(new KeyboardButton("Отмена"))
                ))
                .build();
    }

    private void searchSelection(Context context){
        if(context.text.equals("По номеру квартиры")){
            userCommandManager.updateProgress(context.userCommand, "поиск по номеру квартиры");
            sender.send("Введите номер квартиры", context.update, defaultMarkup());
        }else if(context.text.equals("По номеру машины")){
            userCommandManager.updateProgress(context.userCommand, "поиск по номеру машины");
            sender.send("Введите номер машины", context.update, defaultMarkup());
        }else if(context.text.equals("Отмена")){
            onCancel(context);
        }else {
            onError(context);
        }
    }


    private void findByApartment(Context context){
        if(context.text.equals("Назад")){
            onBack(context);
        }else if(context.text.equals("Отмена")){
            onCancel(context);
        }else {
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
        }
    }

    private void houseSelection(Context context){
        if(context.text.equals("Назад")){
            onBack(context);
        }else if(context.text.equals("Отмена")){
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
                    userCommandManager.updateProgress(context.userCommand, "квартира");
                    sender.send("Введите номер квартиры", context.update, defaultMarkup());
                }else{
                    sender.send("Дом не найден", context.update);
                }

            }else{
                onError(context);
            }
        }
    }

    private void numberInput(Context context){
        if(context.text.equals("Назад")){
            onBack(context);
        }else if(context.text.equals("Отмена")){
            onCancel(context);
        } else{
            String raw = context.update.getMessage().getText();
            if (new NumericCheck(raw).isInteger()) {
                number = Integer.parseInt(context.update.getMessage().getText());
                Optional<Apartment> apartmentOptional =
                        apartmentRepository.findByHouseIdAndNumber(house.getId(), number);

                if(apartmentOptional.isEmpty()){
                    userCommandManager.clear(context.user);
                    sender.send("В квартире никто не зарегистрирован", context.update, defaultMarkup());
                }else {
                    Optional<DwellerApartmentRole> dwellerApartmentRoleOptional =
                            dwellerApartmentRoleRepository.findByApartmentIdAndApartmentRole(apartmentOptional.get().getId(), ApartmentRole.OWNER.getId());
                }
            }else {
                sender.send("Некорректный ввод. \nВведите номер квартиры", context.update, defaultMarkup());
            }
        }
    }

    private void findByTransport(Context context){
        if(context.text.equals("Назад")){
            onBack(context);
        }else if(context.text.equals("Отмена")){
            onCancel(context);
        }else {
//TODO
        }
    }

    private ReplyKeyboardMarkup houseSelectionMarkup(List<String> houses) {
        final ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);

        final List<KeyboardRow> rows = new ArrayList<>();

        for (String house: houses){
            rows.add(new KeyboardRow(new KeyboardButton(house)));
        }
        rows.add(new KeyboardRow(new KeyboardButton("Назад")));
        rows.add(new KeyboardRow(new KeyboardButton("Отмена")));

        markup.setKeyboard(new ArrayList<>(rows));
        return markup;
    }

    @Override
    public Command getCommand() { return Command.FIND_DWELLER; }

    @Override
    public boolean supports(Command command) {
        return command == Command.FIND_DWELLER;
    }
}
