package ua.ukrposhta.models.telegram.keyboard;

import com.google.gson.annotations.SerializedName;

public class InlineKeyboardButton extends ReplyKeyboardButton {
    @SerializedName("callback_data")
    private String callback = "";
    private String url = "";

    public InlineKeyboardButton(String text) {
        super(text);
    }

    public InlineKeyboardButton setCallback(String callback) {
        this.callback = callback;
        return this;
    }

    public InlineKeyboardButton setUrl(String url) {
        this.url = url;
        return this;
    }
}
