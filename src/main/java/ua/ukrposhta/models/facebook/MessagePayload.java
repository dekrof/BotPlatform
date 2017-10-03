package ua.ukrposhta.models.facebook;

import com.google.gson.annotations.SerializedName;

public class MessagePayload {
    @SerializedName("mid")
    private String id;
    @SerializedName("sticker_id")
    private String stickerId;
    private int seq;
    private String text;
    @SerializedName("quick_reply")
    private QuickReply quickReply;

    public String getId() {
        return id;
    }

    public String getStickerId() {
        return stickerId;
    }

    public String getText() {
        return text;
    }

    public QuickReply getQuickReply() {
        return quickReply;
    }
}
