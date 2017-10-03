package ua.ukrposhta.utils.readers;

public enum ButtonPayloadType {
    TEXT("text"),
    URL("url"),
    CALLBACK("callback");

    private final String type;

    ButtonPayloadType(String type) {
        this.type = type;
    }

    public String getButtonPayloadType() {
        return type;
    }
}
