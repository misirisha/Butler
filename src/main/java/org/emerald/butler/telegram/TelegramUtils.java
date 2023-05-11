package org.emerald.butler.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public final class TelegramUtils {
    private TelegramUtils() {}

    public static InlineKeyboardButton createInlineButton(String message, String callbackData) {
        return new InlineKeyboardButton(message, null, callbackData, null, null, null, null, null, null);
    }

    public static InlineKeyboardButton createInlineButton(String message, Object callbackData) {
        return createInlineButton(message, callbackData.toString());
    }
}
