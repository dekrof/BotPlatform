package ua.ukrposhta.utils.readers;

import ua.ukrposhta.models.texts.ButtonPayload;
import ua.ukrposhta.models.texts.ButtonPayloads;
import ua.ukrposhta.models.texts.Message;
import ua.ukrposhta.models.texts.Messages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Optional;

public class ButtonPayloadsReader {
    private static ButtonPayloadsReader instance;
    private ButtonPayloads buttonPayloads;

    private ButtonPayloadsReader() throws JAXBException, NullPointerException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ButtonPayloads.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        File buttonsFile = new File(getClass().getClassLoader().getResource("textConstants/buttonPayloads.xml").getFile());
        buttonPayloads = (ButtonPayloads) unmarshaller.unmarshal(buttonsFile);
    }

    public static ButtonPayloadsReader getInstance() throws JAXBException {
        if (instance == null) {
            instance = new ButtonPayloadsReader();
        }
        return instance;
    }

    public ButtonPayload getButtonPayload(String buttonName) throws Exception {
        Optional<ButtonPayload> buttonPayload = buttonPayloads.getButtonPayloads().stream()
                .filter(msg -> msg.getName().equalsIgnoreCase(buttonName)).findFirst();
        if (!buttonPayload.isPresent()) {
            throw new Exception("Message '" + buttonName + "' is not found.");
        }
        return buttonPayload.get();
    }

    public ButtonPayload getButtonPayload(String buttonName, ButtonPayloadType buttonPayloadType) throws Exception {
        String buttonType = buttonPayloadType.getButtonPayloadType();
        Optional<ButtonPayload> buttonPayload = buttonPayloads.getButtonPayloads().stream()
                .filter(msg -> msg.getName().equalsIgnoreCase(buttonName) && msg.getType().equalsIgnoreCase(buttonType))
                .findFirst();
        if (!buttonPayload.isPresent()) {
            throw new Exception("Button '" + buttonName + "' of type '" + buttonType + "' is not found.");
        }
        return buttonPayload.get();
    }
}
