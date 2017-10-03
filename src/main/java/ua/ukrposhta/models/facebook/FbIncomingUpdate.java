package ua.ukrposhta.models.facebook;

public abstract class FbIncomingUpdate {
    long timestamp;
    Sender sender;
    Recipient recipient;

    public Sender getSender() {
        return sender;
    }

    public Recipient getRecipient() {
        return recipient;
    }
}
