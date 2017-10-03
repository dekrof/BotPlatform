package ua.ukrposhta.models.facebook;

import com.google.gson.annotations.SerializedName;

public class FbMessage extends FbIncomingUpdate {
    @SerializedName("message")
    private MessagePayload payload;

    public String getText() {
        return payload.getText();
    }

    public QuickReply getQuickReply() {
        return payload.getQuickReply();
    }

    public String getId() {
        return payload.getId();
    }

    public String getStickerId() {
        return payload.getStickerId();
    }
}
