package ua.ukrposhta.models.facebook.template;

import com.google.gson.annotations.SerializedName;
import ua.ukrposhta.utils.types.FbTemplateType;

public class AttachmentPayload {
    @SerializedName("template_type")
    private String templateType;
    @SerializedName("elements")
    public PayloadElements payloadElements;

    protected AttachmentPayload(FbTemplateType templateType, PayloadElement payloadElement) {
        this.templateType = templateType.getType();
        payloadElements = new PayloadElements(payloadElement);
    }

    public String getTemplateType() {
        return templateType;
    }

    public PayloadElements getPayloadElements() {
        return payloadElements;
    }
}
