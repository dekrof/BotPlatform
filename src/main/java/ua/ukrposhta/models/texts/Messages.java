package ua.ukrposhta.models.texts;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

@XmlRootElement(name = "messages")
public class Messages {
    @XmlElement(name = "message")
    private Set<Message> messageList;

    public Set<Message> getMessages() {
        return messageList;
    }
}
