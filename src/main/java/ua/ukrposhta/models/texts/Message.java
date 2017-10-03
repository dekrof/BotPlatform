package ua.ukrposhta.models.texts;

import ua.ukrposhta.utils.readers.MessagesReaderNormalizer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "message")
public class Message {
    private String name;
    private String text;

    public String getName() {
        return name;
    }
    @XmlAttribute
    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }
    @XmlElement
    @XmlJavaTypeAdapter(MessagesReaderNormalizer.class)
    public void setText(String text) {
        this.text = text;
    }
}
