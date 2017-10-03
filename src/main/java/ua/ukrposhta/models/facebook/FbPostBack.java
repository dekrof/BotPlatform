package ua.ukrposhta.models.facebook;

import com.google.gson.annotations.SerializedName;
import ua.ukrposhta.models.facebook.buttons.MessagePostback;

public class FbPostBack extends FbIncomingUpdate {
    @SerializedName("postback")
    MessagePostback postBack;

    public MessagePostback getPostBack() {
        return postBack;
    }
}
