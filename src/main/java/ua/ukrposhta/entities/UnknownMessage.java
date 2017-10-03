package ua.ukrposhta.entities;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "unknown_message")
public class UnknownMessage {
    @Id

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;
    @Column(name = "userid")
    private String userId;
    private String request;
    private String response;
    private Date datetime;
    @Column(name = "bot_type")
    private String botType;

    public UnknownMessage() {
        datetime = new Date();
    }

    public UnknownMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UnknownMessage setRequest(String request) {
        this.request = request;
        return this;
    }

    public UnknownMessage setResponse(String response) {
        this.response = response;
        return this;
    }

    public UnknownMessage setBotType(String botType) {
        this.botType = botType;
        return this;
    }
}
