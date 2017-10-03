package ua.ukrposhta.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class ViberUser {
    @Id
    private String id;
    private String name;
    @Column(name = "avatar")
    private String avatarUrl;
    private String language;
    private String country;
    @Column(name = "api_version")
    private String apiVersion;
    @Column(name = "date_add")
    private Date date = new Date();

    public String getId() {
        return id;
    }

    public ViberUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ViberUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public ViberUser setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public ViberUser setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public ViberUser setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public ViberUser setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }
}
