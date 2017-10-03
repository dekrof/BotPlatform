package ua.ukrposhta.models.viber;

import com.google.gson.annotations.SerializedName;

public class VbMessage {
    private long timestamp;
    @SerializedName("message_token")
    private String messageToken;
    private boolean silent;
    private Sender sender;
    private Message message;

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessageToken() {
        return messageToken;
    }

    public boolean isSilent() {
        return silent;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Message getMessage() {
        return message;
    }
}
