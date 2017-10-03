package ua.ukrposhta.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "vw_ind_VPZindex")
public class VPZIndex implements Serializable {

    @Column(name = "city_name")
    private String cityName;

    @Column(name = "region_name")
    private String regionName;

    @Column(name = "oblast_name")
    private String oblastName;

    @Column(name = "code")
    private String postIndex;
    @Id
    @Column(name = "postfilial_number", insertable = false, updatable = false)
    private String postFilialNumber;

    @OneToMany(mappedBy = "vpzIndex", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private List<PostFilial> postFilialList;



    @OneToOne
    @JoinColumn(name = "postfilial_number", referencedColumnName = "postfilial")
    private PostFilialGeo postFilialGeo;

    @OneToOne
    @JoinColumn(name = "city_number", referencedColumnName = "number")
    private City city;

    public VPZIndex() {

    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getOblastName() {
        return oblastName;
    }

    public void setOblastName(String oblastName) {
        this.oblastName = oblastName;
    }

    public String getPostIndex() {
        return postIndex;
    }

    public void setPostIndex(String index) {
        this.postIndex = index;
    }

    public String getPostFilialNumber() {
        return postFilialNumber;
    }

    public void setPostFilialNumber(String postfilialNumber) {
        this.postFilialNumber = postfilialNumber;
    }

    public PostFilial getPostFilial() {
        Optional<PostFilial> result = postFilialList.stream().filter(postFilial -> postFilial.getDateStop().after(new Date())).findAny();
        return result.isPresent() ? result.get() : null;
    }

    public void setPostFilial(List<PostFilial> postFilial) {
        this.postFilialList = postFilial;
    }

    public PostFilialGeo getPostFilialGeo() {
        return postFilialGeo;
    }

    public void setPostFilialGeo(PostFilialGeo postFilialGeo) {
        this.postFilialGeo = postFilialGeo;
    }

    public City getCity() {
        return this.city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VPZIndex vpzIndex = (VPZIndex) o;

        if (!cityName.equals(vpzIndex.cityName)) return false;
        if (!regionName.equals(vpzIndex.regionName)) return false;
        if (!oblastName.equals(vpzIndex.oblastName)) return false;
        if (!postIndex.equals(vpzIndex.postIndex)) return false;
        return postFilialNumber.equals(vpzIndex.postFilialNumber);

    }

    @Override
    public int hashCode() {
        int result = cityName.hashCode();
        result = 31 * result + regionName.hashCode();
        result = 31 * result + oblastName.hashCode();
        result = 31 * result + postIndex.hashCode();
        result = 31 * result + postFilialNumber.hashCode();
        return result;
    }
}
