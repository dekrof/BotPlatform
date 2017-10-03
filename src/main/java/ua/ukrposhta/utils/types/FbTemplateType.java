package ua.ukrposhta.utils.types;

public enum FbTemplateType {
    GENERIC("generic"),
    BUTTON("button");

    private String type;

    private FbTemplateType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
