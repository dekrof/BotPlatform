package ua.ukrposhta.utils.types;

public enum FbAttachmentType {
    TEMPLATE("template");

    private String type;

    private FbAttachmentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
