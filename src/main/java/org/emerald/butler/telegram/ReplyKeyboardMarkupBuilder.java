package org.emerald.butler.telegram;

import java.util.ArrayList;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class ReplyKeyboardMarkupBuilder {
    private List<KeyboardRow> keyboard;
    private Boolean resizeKeyboard;
    private Boolean oneTimeKeyboard;
    private Boolean selective;

    public ReplyKeyboardMarkupBuilder resizeKeyboard(Boolean resizeKeyboard) {
        this.resizeKeyboard = resizeKeyboard;
        return this;
    }

    public ReplyKeyboardMarkupBuilder oneTimeKeyboard(Boolean oneTimeKeyboard) {
        this.oneTimeKeyboard = oneTimeKeyboard;
        return this;
    }

    public ReplyKeyboardMarkupBuilder keyboard(List<? extends KeyboardRow> keyboard) {
        this.keyboard = new ArrayList<>(keyboard);
        return this;
    }

    public ReplyKeyboardMarkupBuilder withKeyboardWrapped(List<org.emerald.butler.telegram.KeyboardRow> keyboard) {
        return this.keyboard(new ArrayList<>(keyboard));
    }

    public ReplyKeyboardMarkupBuilder selective(Boolean selective) {
        this.selective = selective;
        return this;
    }

    public ReplyKeyboardMarkup build() {
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(resizeKeyboard);
        markup.setOneTimeKeyboard(oneTimeKeyboard);
        markup.setSelective(selective);

        return markup;
    }
}
