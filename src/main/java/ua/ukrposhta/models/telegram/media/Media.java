package ua.ukrposhta.models.telegram.media;

import com.google.gson.annotations.SerializedName;
import ua.ukrposhta.models.telegram.keyboard.Keyboard;

public class Media {
    @SerializedName("chat_id")
    private Integer chatId;
    private String photo;
    private String caption;

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void sendPhoto() {

    }
}
