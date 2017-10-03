package ua.ukrposhta.models.viber.keyboard;

import com.google.gson.annotations.SerializedName;

public class UrlButton extends Button{
    @SerializedName("ActionType")
    private String actionType = "open-url";
    @SerializedName("ActionBody")
    private String url;

    public UrlButton() {}

    public UrlButton(int columns, int rows, String text, String url) {
        this.columns = columns;
        this.rows = rows;
        this.text = text;
        this.url = url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
