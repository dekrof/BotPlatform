package ua.ukrposhta.utils.readers;

import ua.ukrposhta.models.texts.Message;
import ua.ukrposhta.models.texts.Messages;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Optional;

public class MessagesReader {
    private static MessagesReader instance;
    private Messages messages;
    private Unmarshaller unmarshaller;

    private MessagesReader() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Messages.class);
        unmarshaller = jaxbContext.createUnmarshaller();
    }

    public static MessagesReader getInstance() throws JAXBException {
        if (instance == null) {
            instance = new MessagesReader();
        }
        return instance;
    }

    public String getMessage(String messageName) throws Exception {
        File messagesFile = new File(getClass().getClassLoader().getResource("textConstants/messages.xml").getFile());
        messages = (Messages)unmarshaller.unmarshal(messagesFile);

        Optional<Message> message = messages.getMessages().stream()
                .filter(msg -> msg.getName().equalsIgnoreCase(messageName)).findFirst();
        if (!message.isPresent()) {
            throw new Exception("Message '" + messageName + "' is not found.");
        }
        return message.get().getText();
    }
}
