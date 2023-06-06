package org.emerald.butler.operation;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.emerald.butler.Constant;
import org.emerald.butler.entity.Command;
import org.emerald.butler.entity.Handbook;
import org.emerald.butler.entity.HandbookCategory;
import org.emerald.butler.entity.HandbookItem;
import org.emerald.butler.repository.HandbookItemRepository;
import org.emerald.butler.service.HandbookService;
import org.emerald.butler.telegram.KeyboardRow;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

@RequiredArgsConstructor
@Component
public class HandbookOperation extends AbstractOperation {
    private final HandbookService handbookService;
    private final HandbookItemRepository handbookItemRepository;

    @Override
    protected Map<String, Consumer<Context>> getProgressesMap() {
        return new ImmutableMap.Builder<String, Consumer<Context>>()
                .put("start", this::onStart)
                .put("меню справочника", this::onHandbookMenu)
                .put("записать данные в справочник", this::onWriteHandbookCategory)
                .put("записать номер телефона аварийной службы", this::onWritePhoneNumber)
                .put("записать дату ремонтных работ", this::onWriteRepairDate)
                .put("записать общедомовое мероприятие", this::onWriteMeeting)
                .build();
    }

    @Override
    public Command getCommand() {
        return Command.HANDBOOK;
    }

    @Override
    public boolean supports(Command command) {
        return getCommand() == command;
    }

    private void onStart(Context context) {
        userCommandManager.updateProgress(context.userCommand, "меню справочника");
        sender.sendToChat("Выберите следующее действие", context.update, handbookMenuMarkup());
    }

    private ReplyKeyboardMarkup handbookMenuMarkup() {
        return markup(List.of(
                new KeyboardRow(new KeyboardButton("Номера телефонов авар. служб")),
                new KeyboardRow(new KeyboardButton("Даты ремонтных работ")),
                new KeyboardRow(new KeyboardButton("Общедомовые мероприятия")),
                new KeyboardRow(new KeyboardButton("Записать")),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        ));
    }

    private void onHandbookMenu(Context context) {
        if (defaultHandle(context).isHandled()) {
            return;
        }

        if (context.isText("Номера телефонов авар. служб")) {
            final List<HandbookItem> items = handbookService.getAllItems(context, HandbookCategory.PHONES);
            items.sort(Comparator.comparing(HandbookItem::getCreatedDate).reversed());

            String message = items.stream().map(HandbookItem::getValue).collect(Collectors.joining("\n"));
            if (message.isBlank()) {
                message = "Нет информации о номерах телефонов аварийных служб";
            }

            sender.sendToChat(message, context.update, handbookMenuMarkup());
        } else if (context.isText("Даты ремонтных работ")) {
            final List<HandbookItem> items = handbookService.getAllItems(context, HandbookCategory.REPAIRS);
            items.sort(Comparator.comparing(HandbookItem::getCreatedDate).reversed());

            String message = items.stream().map(HandbookItem::getValue).collect(Collectors.joining("\n"));
            if (message.isBlank()) {
                message = "Нет информации о датах ремонтных работ";
            }

            sender.sendToChat(message, context.update, handbookMenuMarkup());
        } else if (context.isText("Общедомовые мероприятия")) {
            final List<HandbookItem> items = handbookService.getAllItems(context, HandbookCategory.MEETINGS);
            items.sort(Comparator.comparing(HandbookItem::getCreatedDate).reversed());

            String message = items.stream().map(HandbookItem::getValue).collect(Collectors.joining("\n"));
            if (message.isBlank()) {
                message = "Нет информации об общедомовых мероприятиях";
            }

            sender.sendToChat(message, context.update, handbookMenuMarkup());
        } else if (context.isText("Записать")) {
            userCommandManager.updateProgress(context.userCommand, "записать данные в справочник");
            sender.sendToChat("Выберите следующее действие", context.update, writeHandbookCategoryMarkup());
        } else {
            onError(context);
        }
    }

    private ReplyKeyboardMarkup writeHandbookCategoryMarkup() {
        return markup(List.of(
                new KeyboardRow(new KeyboardButton("Номера телефонов авар. служб")),
                new KeyboardRow(new KeyboardButton("Даты ремонтных работ")),
                new KeyboardRow(new KeyboardButton("Общедомовые мероприятия")),
                new KeyboardRow(new KeyboardButton(Constant.BACK)),
                new KeyboardRow(new KeyboardButton(Constant.CANCEL))
        ));
    }

    private void onWriteHandbookCategory(Context context) {
        if (defaultHandle(context).isHandled()) {
            return;
        }

        if (context.text.equals(Constant.BACK)) {
            onBack(context);
            sender.sendToChat("Выберите следующее действие", context.update, handbookMenuMarkup());
        } else if (context.isText("Номера телефонов авар. служб")) {
            userCommandManager.updateProgress(context.userCommand, "записать номер телефона аварийной службы");
            sender.sendToChat("Выберите следующее действие", context.update, defaultMarkup());
        } else if (context.isText("Даты ремонтных работ")) {
            userCommandManager.updateProgress(context.userCommand, "записать дату ремонтных работ");
            sender.sendToChat("Выберите следующее действие", context.update, defaultMarkup());
        } else if (context.isText("Общедомовые мероприятия")) {
            userCommandManager.updateProgress(context.userCommand, "записать общедомовое мероприятие");
            sender.sendToChat("Выберите следующее действие", context.update, defaultMarkup());
        } else {
            onError(context);
        }
    }

    private void onWritePhoneNumber(Context context) {
        addAbstractHandbookItem(context, HandbookCategory.PHONES);
    }

    private void onWriteRepairDate(Context context) {
        addAbstractHandbookItem(context, HandbookCategory.REPAIRS);
    }

    private void onWriteMeeting(Context context) {
        addAbstractHandbookItem(context, HandbookCategory.MEETINGS);
    }

    private void addAbstractHandbookItem(Context context, String categoryName) {
        if (defaultHandle(context, this::writeHandbookCategoryMarkup).isHandled()) {
            return;
        }

        final Long chatId = context.update.getMessage().getChatId();
        final Handbook handbook = handbookService.getOrCreate(chatId);
        final HandbookCategory phones = handbook.getCategoryByName(categoryName);
        final String newValue = context.text;

        final HandbookItem newItem = handbookService.createItem(phones, newValue);
        handbookItemRepository.save(newItem);

        onBack(context, 2);
        sender.sendToChat("Запись успешно добавлена", context.update, handbookMenuMarkup());
    }
}
