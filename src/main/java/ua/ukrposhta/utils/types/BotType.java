package ua.ukrposhta.utils.types;

public enum BotType {
    TELEGRAM("telegram"),
    FACEBOOK("facebook"),
    VIBER("viber");

    private String name;

    BotType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
