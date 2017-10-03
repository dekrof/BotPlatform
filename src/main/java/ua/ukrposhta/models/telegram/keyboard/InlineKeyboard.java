package ua.ukrposhta.models.telegram.keyboard;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class InlineKeyboard extends Keyboard {
    @SerializedName("inline_keyboard")
    private List<List<InlineKeyboardButton>> keyboard;

    public InlineKeyboard() {
        keyboard = new LinkedList<List<InlineKeyboardButton>>();
    }

    public InlineKeyboard addRow() {
        List row = new LinkedList<InlineKeyboardButton>();
        keyboard.add(row);
        return this;
    }

    public InlineKeyboard addButton(int rowIndex, InlineKeyboardButton button) {
        List<InlineKeyboardButton> row = keyboard.get(rowIndex);
        row.add(button);
        return this;
    }
}
