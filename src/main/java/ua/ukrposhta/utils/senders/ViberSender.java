package ua.ukrposhta.utils.senders;

import com.google.gson.Gson;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import ua.ukrposhta.models.viber.VbMessage;
import ua.ukrposhta.models.viber.keyboard.Button;
import ua.ukrposhta.models.viber.keyboard.Keyboard;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.types.LoggerType;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class ViberSender {
    private static ViberSender instance;
    private BotLogger log = BotLogger.getLogger(LoggerType.VIBER);

    private String baseUrl;
    private String botToken;
    private String receiver;
    private String text;
    private Keyboard keyboard;
    private String latitude;
    private String longitude;
    private String pictureUrl;

    private ViberSender() {
        baseUrl = System.getProperty("viberBot.baseUrl");
        botToken = System.getProperty("viberBot.botToken");
    }

    public static ViberSender getInstance() {
        if (instance == null) {
            instance = new ViberSender();
        }
        return instance;
    }

    public ViberSender setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public ViberSender addButton(Button button) {
        if (keyboard == null) {
            keyboard = new Keyboard();
        }
        keyboard.addButton(button);

        return this;
    }

    public ViberSender setReceiver(String receiver) {
        this.receiver = receiver;
        return this;
    }

    public ViberSender setText(String text) {
        this.text = text;
        return this;
    }

    public ViberSender setCoordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        return this;
    }

    public ViberSender setPicture(String pictureUrl) {
        this.pictureUrl = pictureUrl;
        return this;
    }

    public void sendLocation() {
        JSONObject locationJson = new JSONObject()
                .put("lat", latitude)
                .put("lon", longitude);
        JSONObject message = new JSONObject()
                .put("receiver", receiver)
                .put("type", "location")
                .put("location", locationJson);
        if (keyboard != null) {
            JSONObject keyboardJson = new JSONObject(new Gson().toJson(keyboard));
            message.put("keyboard", keyboardJson);
        }
        send(message);
    }

    public void sendPicture() {
        JSONObject message = new JSONObject()
                .put("receiver", receiver)
                .put("type", "picture")
                .put("text", text)
                .put("media", pictureUrl);
        send(message);
    }

    public void sendMessage() {
        JSONObject message = new JSONObject()
                .put("receiver", receiver);
        if (text != null && !text.isEmpty()) {
            message.put("type", "text")
                    .put("text", text);
        }
        if (keyboard != null) {
            JSONObject keyboardJson = new JSONObject(new Gson().toJson(keyboard));
            message.put("keyboard", keyboardJson);
        }
        send(message);
    }

    private void send(JSONObject payload) {
        String finalUrl = baseUrl
                + (baseUrl.endsWith("/") ? "send_message" : "/send_message");
        try {
            URL url = new URL(finalUrl);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestProperty("X-Viber-Auth-Token", botToken);
            httpsURLConnection.setRequestProperty("charset", "UTF-8");
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setDoOutput(true);

            StringEntity stringEntity = new StringEntity(payload.toString(), "UTF-8");
            stringEntity.writeTo(httpsURLConnection.getOutputStream());

            log.info("Sending message to viber: " + payload.toString());
            httpsURLConnection.getOutputStream().flush();

            int HttpResult = httpsURLConnection.getResponseCode();
            if (HttpResult == HttpsURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(httpsURLConnection.getInputStream(), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line)
                            .append("\n");
                }
                br.close();
                log.info("Viber response: " + sb.toString());
            } else {
                log.warn("Viber response: " + httpsURLConnection.getResponseMessage());
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            cleanUp();
        }
    }

    private void cleanUp() {
        keyboard = null;
        receiver = null;
        text = null;
        latitude = null;
        longitude = null;
    }
}
