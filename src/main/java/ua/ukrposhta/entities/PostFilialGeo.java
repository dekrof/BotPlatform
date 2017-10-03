package ua.ukrposhta.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pn_postfilialgeo")
public class PostFilialGeo {
    private String latitude;
    private String longitude;
    @Id
    @Column(name = "postfilial")
    private String postFilial;

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getPostFilial() {
        return postFilial;
    }

    public void setPostFilial(String postFilial) {
        this.postFilial = postFilial;
    }
}