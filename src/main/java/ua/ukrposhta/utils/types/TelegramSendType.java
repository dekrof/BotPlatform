package ua.ukrposhta.utils.types;

public enum TelegramSendType {
    SEND_MESSAGE("sendMessage"),
    SEND_PHOTO("sendPhoto"),
    SEND_LOCATION("sendLocation"),
    EDIT_MESSAGE("editMessageText");

    private final String methodName;

    TelegramSendType(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
