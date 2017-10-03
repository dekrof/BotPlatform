package ua.ukrposhta.models.telegram.keyboard;

import com.google.gson.annotations.SerializedName;

import java.util.*;

public class ReplyKeyboard extends Keyboard {
    @SerializedName("keyboard")
    private List<List<ReplyKeyboardButton>> keyboard;
    @SerializedName("one_time_keyboard")
    private boolean oneTimeKeyboard;
    @SerializedName("resize_keyboard")
    private boolean resizeKeyboard;

    public ReplyKeyboard() {
        keyboard = new LinkedList<>();
    }

    public ReplyKeyboard setResizeKeyboard(boolean resizeKeyboard) {
        this.resizeKeyboard = resizeKeyboard;
        return this;
    }

    public ReplyKeyboard setOneTimeKeyboard(boolean oneTimeKeyboard) {
        this.oneTimeKeyboard = oneTimeKeyboard;
        return this;
    }

    public ReplyKeyboard addRow() {
        List row = new LinkedList<ReplyKeyboardButton>();
        keyboard.add(row);
        return this;
    }

    public ReplyKeyboard addButton(int rowIndex, ReplyKeyboardButton button) {
        List<ReplyKeyboardButton> row = keyboard.get(rowIndex);
        row.add(button);
        return this;
    }
}
