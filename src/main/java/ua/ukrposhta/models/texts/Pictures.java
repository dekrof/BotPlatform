package ua.ukrposhta.models.texts;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "pictures")
public class Pictures {
    private List<Picture> pictureList;

    public List<Picture> getPictureList() {
        return pictureList;
    }

    @XmlElement(name = "picture")
    public void setPictureList(List<Picture> pictureList) {
        this.pictureList = pictureList;
    }
}
