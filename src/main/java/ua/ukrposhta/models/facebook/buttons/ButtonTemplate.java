package ua.ukrposhta.models.facebook.buttons;

import org.json.JSONArray;
import org.json.JSONObject;

public class ButtonTemplate {
    private JSONObject attachment;
    private JSONObject payload;
    private JSONArray buttons;

    public ButtonTemplate(String message) {
        buttons = new JSONArray();
        payload = new JSONObject()
                .put("template_type", "button")
                .put("text", message)
                .put("buttons", buttons);
        attachment = new JSONObject()
                .put("payload", payload)
                .put("type", "template");
    }

    public ButtonTemplate addUrlButton(String title, String url) {
        buttons.put(new JSONObject()
                .put("type", "web_url")
                .put("title", title)
                .put("url", url));
        return this;
    }

    public JSONObject getJsonObject() {
        return new JSONObject()
                .put("attachment", attachment);
    }
}
