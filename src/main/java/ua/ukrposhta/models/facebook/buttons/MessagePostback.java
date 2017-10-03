package ua.ukrposhta.models.facebook.buttons;

import com.google.gson.annotations.SerializedName;

public class MessagePostback {
    @SerializedName("payload")
    private String text;
    private String title;

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }
}
