package ua.ukrposhta.models.viber;

import com.google.gson.annotations.SerializedName;

public class Message {
    private String text;
    private String type;
    private String media;
    private String thumbnail;
    @SerializedName("tracking_data")
    private String trackingData;

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public String getMedia() {
        return media;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getTrackingData() {
        return trackingData;
    }
}
