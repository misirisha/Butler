package org.emerald.butler.telegram;

import java.util.Collection;
import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

public class KeyboardRow extends org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow {
    public KeyboardRow(int initialCapacity) {
        super(initialCapacity);
    }

    public KeyboardRow() {
        super();
    }

    public KeyboardRow(Collection<? extends KeyboardButton> c) {
        super(c);
    }

    public KeyboardRow(KeyboardButton... buttons) {
        super(List.of(buttons));
    }
}
