package ua.ukrposhta.utils.readers;

import ua.ukrposhta.models.texts.ButtonPayload;
import ua.ukrposhta.models.texts.Picture;
import ua.ukrposhta.models.texts.Pictures;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Optional;

public class PicturesReader {
    private static PicturesReader instance;
    private Pictures pictures;

    private PicturesReader() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Pictures.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        File picturesFile = new File(getClass().getClassLoader().getResource("textConstants/pictures.xml").getFile());
        pictures = (Pictures) unmarshaller.unmarshal(picturesFile);
    }

    public static PicturesReader getInstance() throws JAXBException {
        if (instance == null) {
            instance = new PicturesReader();
        }
        return instance;
    }

    public String getPictureUrl(String pictureName) throws Exception {
        Optional<Picture> picture = pictures.getPictureList().stream()
                .filter(msg -> msg.getName().equalsIgnoreCase(pictureName)).findFirst();
        if (!picture.isPresent()) {
            throw new Exception("Message '" + pictureName + "' is not found.");
        }
        return picture.get().getUrl();
    }
}
