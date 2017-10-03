package ua.ukrposhta.models.facebook;

import com.google.gson.annotations.SerializedName;

public class QuickReply {
    @SerializedName("payload")
    private String text;

    public String getText() {
        return text;
    }
}
