package ua.ukrposhta.models.texts;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "buttons")
public class ButtonPayloads {
    private List<ButtonPayload> buttonPayloads;

    public List<ButtonPayload> getButtonPayloads() {
        return buttonPayloads;
    }

    @XmlElement(name = "button")
    public void setButtonPayloads(List<ButtonPayload> buttons) {
        this.buttonPayloads = buttons;
    }
}
