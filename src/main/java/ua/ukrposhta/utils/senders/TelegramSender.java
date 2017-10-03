package ua.ukrposhta.utils.senders;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import ua.ukrposhta.models.telegram.keyboard.Keyboard;
import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.types.TelegramSendType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// TODO: 07.08.2017 refactor code similarly to FacebookSender (get rid of NameValuePair)
public class TelegramSender {
    private static TelegramSender instance;
    private BotLogger log = BotLogger.getLogger(LoggerType.TELEGRAM);

    private String baseUrl;
    private String botUsername;
    private String botToken;

    private final String CONTENT_TYPE = "application/x-www-form-urlencoded";
    private final String CHARSET = "UTF-8";
    private String chatId;
    private String messageId;
    private String text;
    private String photo;
    private String latitude;
    private String longitude;
    private Keyboard replyKeyboard;

    public TelegramSender setMessageId(String messageId) {
        this.messageId = messageId;
        return instance;
    }

    public TelegramSender setText(String text) {
        this.text = text;
        return instance;
    }

    public TelegramSender setPhoto(String photo) {
        this.photo = photo;
        return instance;
    }

    public TelegramSender setLatitude(String latitude) {
        this.latitude = latitude;
        return instance;
    }

    public TelegramSender setLongitude(String longitude) {
        this.longitude = longitude;
        return instance;
    }

    public TelegramSender setChatId(String chatId) {
        this.chatId = chatId;
        return instance;
    }

    public TelegramSender setReplyMarkup(Keyboard replyKeyboard) {
        this.replyKeyboard = replyKeyboard;
        return instance;
    }

    private TelegramSender() {
        baseUrl = System.getProperty("telegramBot.baseUrl");
        botUsername = System.getProperty("telegramBot.botUsername");

    }

    public static TelegramSender getInstance() {
        if (instance == null) {
            instance = new TelegramSender();
        }
        return instance;
    }

    public void sendRedirect() {
        botToken = System.getProperty("telegramBot.oldBotToken");
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("chat_id", chatId));
        nameValuePairs.add(new BasicNameValuePair("text", text));
        send(nameValuePairs, TelegramSendType.SEND_MESSAGE);
    }

    public void sendMessage() {
        botToken = System.getProperty("telegramBot.botToken");
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("chat_id", chatId));
        nameValuePairs.add(new BasicNameValuePair("text", text));
        nameValuePairs.add(new BasicNameValuePair("parse_mode", "Markdown"));
        if (replyKeyboard != null) {
            nameValuePairs.add(new BasicNameValuePair("reply_markup", new Gson().toJson(replyKeyboard)));
        }
        send(nameValuePairs, TelegramSendType.SEND_MESSAGE);
    }

    public void sendPhoto() {
        botToken = System.getProperty("telegramBot.botToken");
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("chat_id", chatId));
        nameValuePairs.add(new BasicNameValuePair("photo", photo));
        if (replyKeyboard != null) {
            nameValuePairs.add(new BasicNameValuePair("reply_markup", new Gson().toJson(replyKeyboard)));
        }
        send(nameValuePairs, TelegramSendType.SEND_PHOTO);
        cleanUp();
    }

    public void sendLocation() {
        botToken = System.getProperty("telegramBot.botToken");
        ArrayList nameValuePairs = new ArrayList();
        nameValuePairs.add(new BasicNameValuePair("chat_id", this.chatId));
        nameValuePairs.add(new BasicNameValuePair("latitude", this.latitude));
        nameValuePairs.add(new BasicNameValuePair("longitude", this.longitude));
        if (replyKeyboard != null) {
            nameValuePairs.add(new BasicNameValuePair("reply_markup", new Gson().toJson(replyKeyboard)));
        }
        this.send(nameValuePairs, TelegramSendType.SEND_LOCATION);
        cleanUp();
    }

    public void editMessage() {
        botToken = System.getProperty("telegramBot.botToken");
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("chat_id", chatId));
        nameValuePairs.add(new BasicNameValuePair("message_id", messageId));
        nameValuePairs.add(new BasicNameValuePair("text", text));
        nameValuePairs.add(new BasicNameValuePair("parse_mode", "Markdown"));
        if (replyKeyboard != null) {
            nameValuePairs.add(new BasicNameValuePair("reply_markup", new Gson().toJson(replyKeyboard)));
        }
        send(nameValuePairs, TelegramSendType.EDIT_MESSAGE);
        cleanUp();
    }
    
    private void send(List<NameValuePair> payload, TelegramSendType sendMethod) {
        List<NameValuePair> unsetElements = payload.stream().filter(payloadItem -> payloadItem.getValue() == null ||
                payloadItem.getValue().isEmpty()).collect(Collectors.toList());
        unsetElements.forEach(unsetElement -> log.error("\"" + unsetElement.getName() + "\" is not set in response."));
        if (unsetElements.size() > 0) {
            log.error("Aborting sending to telegram.");
            cleanUp();
            return;
        }

        String url = baseUrl
                + (baseUrl.endsWith("/") ? "bot" : "/bot")
                + botToken
                + "/" + sendMethod.getMethodName();

        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(url);
        httppost.addHeader("Content-type", CONTENT_TYPE);
        httppost.addHeader("charset", CHARSET);

        try {
            UrlEncodedFormEntity encodedEntity = new UrlEncodedFormEntity(payload, "UTF-8");
            httppost.setEntity(encodedEntity);
            log.info("Sending message to telegram: " + new Gson().toJson(payload));
            HttpResponse response = httpclient.execute(httppost);

            HttpEntity entity = response.getEntity();

            if (entity != null) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(entity.getContent(), "UTF-8"));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                log.info("telegram response: " + new JSONObject(content.toString()).toString());
            }
        } catch (IOException|JSONException e) {
            log.error(e);
        }
        cleanUp();
    }

    private void cleanUp() {
        chatId = null;
        messageId = null;
        text = null;
        latitude = null;
        longitude = null;
        replyKeyboard = null;
    }
}
