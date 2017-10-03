package ua.ukrposhta.entities;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "vw_routineofwork")
public class WorkSchedule implements Serializable {

    private String days;
    @Id
    @Column(name = "short_days")
    private String shortDays;
    @Id
    @Column(name = "workfrom")
    private String workFrom;
    @Id
    @Column(name = "workto")
    private String workTo;
    @Id
    @Column(name = "postfilial", insertable = false, updatable = false)
    private String postFilialNumber;

    public WorkSchedule() {

    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getShortDays() {
        return shortDays;
    }

    public void setShortDays(String shortDays) {
        this.shortDays = shortDays;
    }

    public String getWorkFrom() {
        return workFrom;
    }

    public void setWorkFrom(String workFrom) {
        this.workFrom = workFrom;
    }

    public String getWorkTo() {
        return workTo;
    }

    public void setWorkTo(String workTo) {
        this.workTo = workTo;
    }

    public String getPostFilialNumber() {
        return postFilialNumber;
    }

    public void setPostFilialNumber(String postfilialNumber) {
        this.postFilialNumber = postfilialNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WorkSchedule that = (WorkSchedule) o;

        if (!days.equals(that.days)) return false;
        if (!shortDays.equals(that.shortDays)) return false;
        if (!workFrom.equals(that.workFrom)) return false;
        if (!workTo.equals(that.workTo)) return false;
        return postFilialNumber.equals(that.postFilialNumber);

    }

    @Override
    public int hashCode() {
        int result = days.hashCode();
        result = 31 * result + shortDays.hashCode();
        result = 31 * result + workFrom.hashCode();
        result = 31 * result + workTo.hashCode();
        result = 31 * result + postFilialNumber.hashCode();
        return result;
    }
}
