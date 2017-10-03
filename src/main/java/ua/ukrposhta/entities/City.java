package ua.ukrposhta.entities;

import javax.persistence.Column;

@javax.persistence.Entity
@javax.persistence.Table(name="vw_cd_city")
public class City
{
    @javax.persistence.Id
    @Column(name="number")
    private String cityNumber;
    @Column(name="phonecode")
    private String phoneCode;

    public City() {}

    public String getCityNumber()
    {
        return cityNumber;
    }

    public void setCityNumber(String cityNumber) {
        this.cityNumber = cityNumber;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }
}
