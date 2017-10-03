package ua.ukrposhta.entities;

import javax.persistence.*;
import java.util.Date;

@Entity
public class FacebookUser {
    @Id
    private String userId;

    private String userFirstName;

    private String userLastName;

    @Column(name = "date_add")
    private Date date;

    public FacebookUser() {

    }

    public FacebookUser(String userId, String userFirstName, String userLastName) {
        this.userId = userId;
        this.userFirstName = userFirstName;
        this.userLastName = userLastName;
        this.date = new Date();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
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

    public Date getDateTime() {
        return date;
    }
}
