package ua.ukrposhta.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "pn_postfilial")
public class PostFilial implements Serializable {
    @Id
    private String id;
    private String address;
    private String phone;
    private String code;
    @Column(name = "techindex")
    private String techIndex;
    @Column(name = "datestop")
    private Date dateStop;
    @Column(insertable = false, updatable = false)
    private String number;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "number")
    private VPZIndex vpzIndex;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "postfilial", referencedColumnName = "number")
    private List<WorkSchedule> workScheduleList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTechIndex() {
        return techIndex;
    }

    public void setTechIndex(String techIndex) {
        this.techIndex = techIndex;
    }

    public Date getDateStop() {
        return dateStop;
    }

    public void setDateStop(Date dateStop) {
        this.dateStop = dateStop;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public VPZIndex getVpzIndex() {
        return vpzIndex;
    }

    public void setVpzIndex(VPZIndex vpzIndex) {
        this.vpzIndex = vpzIndex;
    }

    public List<WorkSchedule> getWorkScheduleList() {
        return workScheduleList;
    }
}

