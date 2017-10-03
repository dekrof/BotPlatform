package ua.ukrposhta.models.facebook.template;

import com.google.gson.annotations.SerializedName;

public class PayloadElements {
    @SerializedName("element")
    private PayloadElement payloadElement;

    protected PayloadElements(PayloadElement payloadElement) {
        this.payloadElement = payloadElement;
    }

    public PayloadElement getPayloadElement() {
        return payloadElement;
    }

    public void addPayloadElement(PayloadElement payloadElement) {
        this.payloadElement = payloadElement;
    }
}
