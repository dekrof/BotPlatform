package ua.ukrposhta.entities;

import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class TelegramUser {
    @Id
    private int userId;

    private String userFirstName;

    private String userLastName;

    private long chatId;
    @Column(name = "date_add")
    private Date dateTime;

    public TelegramUser() {
    }

    public TelegramUser(int userId, String userFirstName, String userLastName, long chatId) {
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.chatId = chatId;
        dateTime = new Date();
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getUserLastName() {
        return userLastName;
    }

    public void setUserLastName(String userLastName) {
        this.userLastName = userLastName;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public Date getDateTime() {
        return dateTime;
    }
}
