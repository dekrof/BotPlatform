package ua.ukrposhta.models.viber.keyboard;

import com.google.gson.annotations.SerializedName;

public class ReplyButton extends Button {
    @SerializedName("ActionType")
    private String actionType = "reply";
    @SerializedName("ActionBody")
    private String callBack;

    public ReplyButton() {}

    public ReplyButton(int columns, int rows, String text, String callBack) {
        this.columns = columns;
        this.rows = rows;
        this.text = text;
        this.callBack = callBack;
    }

    public void setCallBack(String callBack) {
        this.callBack = callBack;
    }
}
