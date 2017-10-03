package ua.ukrposhta.models.telegram.keyboard;

import com.google.gson.annotations.SerializedName;

public class ReplyKeyboardButton {
    private String text;

    public ReplyKeyboardButton(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
