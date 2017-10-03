package ua.ukrposhta.models.telegram;

import com.google.gson.annotations.SerializedName;

public class Callback {
    private String id;
    @SerializedName("from")
    private Sender sender;
    private TgMessage message;
    @SerializedName("chat_instance")
    private String chatInstance;
    @SerializedName("data")
    private String text;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Sender getSender() {
        return sender;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public TgMessage getMessage() {
        return message;
    }

    public void setMessage(TgMessage message) {
        this.message = message;
    }

    public String getChatInstance() {
        return chatInstance;
    }

    public void setChatInstance(String chatInstance) {
        this.chatInstance = chatInstance;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
