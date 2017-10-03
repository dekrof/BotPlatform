package ua.ukrposhta.models.facebook.template;

import com.google.gson.annotations.SerializedName;
import ua.ukrposhta.utils.types.FbAttachmentType;
import ua.ukrposhta.utils.types.FbTemplateType;

public class Attachment {
    private String type;
    @SerializedName("payload")
    private AttachmentPayload attachmentPayload;

    protected Attachment(FbAttachmentType type, FbTemplateType templateType, PayloadElement payloadElement) {
        this.type = type.getType();
        attachmentPayload = new AttachmentPayload(templateType, payloadElement);
    }

    public String getType() {
        return type;
    }

    public AttachmentPayload getAttachmentPayload() {
        return attachmentPayload;
    }

    public void setAttachmentPayload(AttachmentPayload attachmentPayload) {
        this.attachmentPayload = attachmentPayload;
    }
}
