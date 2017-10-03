package ua.ukrposhta.models.facebook.template;

import ua.ukrposhta.utils.types.FbAttachmentType;
import ua.ukrposhta.utils.types.FbTemplateType;

public class GenericTemplate {
    private Attachment attachment;

    public GenericTemplate(PayloadElement payloadElement) {
        attachment = new Attachment(FbAttachmentType.TEMPLATE, FbTemplateType.GENERIC, payloadElement);
    }

    public Attachment getAttachment() {
        return attachment;
    }
}
