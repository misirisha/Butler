package org.emerald.butler.component;

import java.io.Serializable;

import org.emerald.butler.util.Format;
import org.emerald.butler.util.Utils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

public abstract class Sender extends TelegramLongPollingBot {
    public void send(Format format, Update chatSource) {
        send(format.get(), chatSource);
    }

    @Override
    public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) {
        return Utils.silently(() -> super.execute(method));
    }

    public void sendToChat(String text, Update chatSource) {
        Utils.silently(() -> {
            execute(toChatSendMessage(text, chatSource.getMessage().getChatId()));
        });
    }

    public void sendToChat(String text, Update chatSource, ReplyKeyboardMarkup markup) {
        Utils.silently(() -> {
            execute(toChatSendMessage(text, chatSource, markup));
        });
    }

    public void send(Format format, Update chatSource, ReplyKeyboardMarkup markup) {
        send(format.get(), chatSource, markup);
    }

    public void send(String text, Update chatSource) {
        Utils.silently(() -> execute(toSendMessage(text, chatSource)));
    }

    public void send(String text, Update chatSource, ReplyKeyboardMarkup markup) {
        Utils.silently(() -> execute(toSendMessage(text, chatSource, markup)));
    }

    private String toChatId(Update update) {
        return update.getMessage().getFrom().getId().toString();
    }

    private SendMessage toSendMessage(String text, Update chatSource) {
        return toSendMessage(text, chatSource, null);
    }

    private SendMessage toSendMessage(String text, Update chatSource, ReplyKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(toChatId(chatSource));
        message.setReplyMarkup(markup);
        return message;
    }

    private SendMessage toChatSendMessage(String text, Object chatId) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId.toString());
        message.setReplyMarkup(null);

        return message;
    }

    private SendMessage toChatSendMessage(String text, Update dataSource, ReplyKeyboardMarkup markup) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(dataSource.getMessage().getChatId().toString());
        message.setReplyMarkup(markup);
        message.setReplyToMessageId(dataSource.getMessage().getMessageId());

        return message;
    }
}
