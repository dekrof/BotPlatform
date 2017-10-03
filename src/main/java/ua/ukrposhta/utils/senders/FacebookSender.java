package ua.ukrposhta.utils.senders;

import com.google.gson.Gson;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import ua.ukrposhta.models.facebook.buttons.ButtonTemplate;
import ua.ukrposhta.models.facebook.template.GenericTemplate;
import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class FacebookSender {
    private static FacebookSender instance;
    private BotLogger log = BotLogger.getLogger(LoggerType.FACEBOOK);

    private String baseUrl;
    private String pageToken;
    private String recipientId;
    private String text;
    private JSONArray quickReplyButtons;
    private JSONObject buttonTemplate;
    private JSONObject genericTemplate;

    private FacebookSender() {
        baseUrl = System.getProperty("facebookBot.baseUrl");
        pageToken = System.getProperty("facebookBot.pageToken");

        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        //No need to implement.
                    }

                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        //No need to implement.
                    }
                }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static FacebookSender getInstance() {
        if (instance == null) {
            instance = new FacebookSender();
        }
        return instance;
    }

    public FacebookSender setRecipientId(String recipientId) {
        this.recipientId = recipientId;
        return this;
    }

    public FacebookSender setText(String text) {
        this.text = text;
        return this;
    }

    public FacebookSender addQuickReply(String title, String payload) {
        if (quickReplyButtons == null) {
            quickReplyButtons = new JSONArray();
        }
        JSONObject newButton = new JSONObject()
                .put("content_type", "text")
                .put("title", title)
                .put("payload", payload);
        quickReplyButtons.put(newButton);

        return this;
    }

    public FacebookSender addButtonTemplate(ButtonTemplate buttonTemplate) {
        this.buttonTemplate = buttonTemplate.getJsonObject();
        return this;
    }

    public FacebookSender addGenericTemplate(GenericTemplate genericTemplate) {
        this.genericTemplate = new JSONObject(new Gson().toJson(genericTemplate));
        return this;
    }

    public void sendLocation() {
        JSONObject message = new JSONObject();
        JSONObject recipientJson = new JSONObject().put("id", recipientId);
        message.put("recipient", recipientJson);
        JSONObject messageContent = new JSONObject()
                .put("quick_replies", quickReplyButtons)
                .put("attachment", genericTemplate.getJSONObject("attachment"));
        message.put("message", messageContent);
        send(message);
    }

    public void sendPicture(String pictureUrl) {
        JSONObject message = new JSONObject();
        JSONObject recipientJson = new JSONObject().put("id", recipientId);
        message.put("recipient", recipientJson);
        JSONObject payload = new JSONObject().put("url", pictureUrl);
        JSONObject attachment = new JSONObject()
                .put("type", "image")
                .put("payload", payload);
        JSONObject msg = new JSONObject().put("attachment", attachment);
        message.put("message", msg);
        send(message);
    }

    public void sendMessage() {
        JSONObject message = new JSONObject();
        JSONObject recipientJson = new JSONObject().put("id", recipientId);
        message.put("recipient", recipientJson);

        JSONObject messageContent = new JSONObject()
                .put("quick_replies", quickReplyButtons);
        if (buttonTemplate != null) {
            messageContent.put("attachment", buttonTemplate.get("attachment"));
        } else {
            messageContent.put("text", text);
        }
        message.put("message", messageContent);
        send(message);
    }

    private void send(JSONObject payload) {
        String finalUrl = baseUrl
                + (baseUrl.endsWith("/") ? "messages?access_token=" : "/messages?access_token=")
                + pageToken;
        try {
            URL url = new URL(finalUrl);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestProperty("charset", "UTF-8");
            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setDoOutput(true);

            StringEntity stringEntity = new StringEntity(payload.toString(), "UTF-8");
            stringEntity.writeTo(httpsURLConnection.getOutputStream());

            log.info("Sending message to facebook: " + payload.toString());
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
                log.info("Facebook response: " + new JSONObject(sb.toString()).toString());
            } else {
                log.warn("Facebook response: " + new JSONObject(httpsURLConnection.getResponseMessage()).toString());
            }
        } catch (IOException e) {
            log.error(e);
        } finally {
            cleanUp();
        }
    }

    private void cleanUp() {
        recipientId = null;
        text = null;
        quickReplyButtons = null;
        buttonTemplate = null;
    }
}
