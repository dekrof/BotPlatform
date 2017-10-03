package ua.ukrposhta.models.texts;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "button")
public class ButtonPayload {
    private String name;
    private String type;
    private String caption;
    private String url;
    private String callback;
    private String description;

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "name")
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    @XmlAttribute(name = "type")
    public void setType(String type) {
        this.type = type;
    }

    public String getCaption() {
        return caption;
    }

    @XmlElement(name = "caption")
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getUrl() {
        return url;
    }

    @XmlElement(name = "url")
    public void setUrl(String url) {
        this.url = url;
    }

    public String getCallback() {
        return callback;
    }

    @XmlElement(name = "callback")
    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getDescription() {
        return description;
    }
    @XmlElement(name = "description")
    public void setDescription(String description) {
        this.description = description;
    }
}
