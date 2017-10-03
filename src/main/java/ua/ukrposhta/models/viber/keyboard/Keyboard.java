package ua.ukrposhta.models.viber.keyboard;

import com.google.gson.annotations.SerializedName;

import java.util.LinkedList;
import java.util.List;

public class Keyboard {
    @SerializedName("Type")
    private String type = "keyboard";
    @SerializedName("DefaultHeight")
    private boolean defaultHeight = false;
    @SerializedName("Buttons")
    private List<Button> buttons;

    public Keyboard addButton(Button button) {
        if (buttons == null) {
            buttons = new LinkedList<>();
        }
        buttons.add(button);

        return this;
    }
}
