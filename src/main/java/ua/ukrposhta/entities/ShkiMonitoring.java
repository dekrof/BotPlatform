package ua.ukrposhta.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "shki_monitoring", schema = "public")
public class ShkiMonitoring implements Serializable {
    @Id
    private String barcode;
    @Id
    private String userId;

    private String status;
    @Id
    private String botType;

    @Column(name = "date_start")
    private Date dateStart;

    @Column(name = "date_update")
    private Date dateUpdate;

    public ShkiMonitoring() {
    }

    public ShkiMonitoring(String barcode, String userId, String status, String botType) {
        this.barcode = barcode;
        this.userId = userId;
        this.status = status;
        this.botType = botType;
        dateStart = new Date();
        dateUpdate = new Date();
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBotType() {
        return botType;
    }

    public void setBotType(String botType) {
        this.botType = botType;
    }

    public Date getDateStart() {
        return dateStart;
    }

    public Date getDateUpdate() {
        return dateUpdate;
    }
}
