package ua.ukrposhta.bot;

import com.google.gson.Gson;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import ua.ukrposhta.models.facebook.FbMessage;
import ua.ukrposhta.models.facebook.FbPostBack;
import ua.ukrposhta.models.telegram.Callback;
import ua.ukrposhta.models.telegram.TgMessage;
import ua.ukrposhta.models.viber.Sender;
import ua.ukrposhta.models.viber.VbMessage;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.ShkiMonitor;
import ua.ukrposhta.utils.leaderNode.LNClient;
import ua.ukrposhta.utils.leaderNode.LNServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

@Controller
public class BotController {
    private BotLogger telegramLog = BotLogger.getLogger(LoggerType.TELEGRAM);
    private BotLogger facebookLog = BotLogger.getLogger(LoggerType.FACEBOOK);
    private BotLogger viberLog = BotLogger.getLogger(LoggerType.VIBER);
    private BotLogger monitorLog = BotLogger.getLogger(LoggerType.MONITOR);
    private TelegramBot telegramBot;
    private FacebookBot facebookBot;
    private ViberBot viberBot;
    private Thread lnServer;
    private Thread lnClient;

    @PostConstruct
    public void init() {
        System.out.println("Starting bot");
        telegramBot = TelegramBot.getInstance();
        telegramLog.info("telegram bot started.");
        facebookBot = FacebookBot.getInstance();
        facebookLog.info("Facebook bot started.");
        viberBot = ViberBot.getInstance();
        viberLog.info("Viber bot started.");

        lnServer = LNServer.getInstance();
        lnServer.start();
        lnClient = LNClient.getInstance();
        lnClient.start();
    }

    @RequestMapping(value = "/telegram", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public void getRequest() {
    }

    @RequestMapping(value = "/telegram", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTelegramUpdates(@RequestBody String body) {
        JSONObject jsonBody = new JSONObject(body);
        telegramLog.info("Received a message from telegram: " + jsonBody.toString());
        try {
            if (jsonBody.has("callback_query")) {
                JSONObject jsonCallback = jsonBody.getJSONObject("callback_query");
                Callback callback = new Gson().fromJson(jsonCallback.toString(), Callback.class);
                telegramBot.handleCallback(callback);
            } else {
                JSONObject jsonMessage = jsonBody.getJSONObject("message");
                TgMessage message = new Gson().fromJson(jsonMessage.toString(), TgMessage.class);
                telegramBot.handleMessage(message);
            }
        } catch (Exception e) {
            telegramLog.error(e);
        }
    }

    @RequestMapping(value = "/telegram/redirect", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getTelegramUpdatesOld(@RequestBody String body) {
        telegramLog.info("Received a message from old telegram bot: " + new JSONObject(body).toString());
        JSONObject jsonMessage = new JSONObject(body).getJSONObject("message");
        TgMessage message = new Gson().fromJson(jsonMessage.toString(), TgMessage.class);
        try {
            telegramBot.handleRedirect(message);
        } catch (Exception e) {
            telegramLog.error(e);
        }
    }

    @RequestMapping(value = "/facebook", method = RequestMethod.GET)
    public ResponseEntity<?> confirmWebHook(HttpServletRequest request) {
        String validationToken = System.getProperty("facebookBot.validationToken");
        String requestMode = request.getParameter("hub.mode");
        String requestToken = request.getParameter("hub.verify_token");
        String requestChallenge = request.getParameter("hub.challenge");
        if (requestMode.equals("subscribe") && requestToken.equals(validationToken)) {
            facebookLog.info("Facebook webhook confirmed.");
            return new ResponseEntity<>(requestChallenge, HttpStatus.OK);
        } else {
            facebookLog.error("GET request is not a subscription request or it has wrong validation token.");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @RequestMapping(value = "/facebook", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getFacebookUpdates(@RequestBody String body) {
        facebookLog.info("Received a message from facebook: " + new JSONObject(body).toString());
        try {
            JSONObject messageJson = new JSONObject(body)
                    .getJSONArray("entry")
                    .getJSONObject(0)
                    .getJSONArray("messaging")
                    .getJSONObject(0);
            if (messageJson.has("postback")) {
                FbPostBack postBack = new Gson().fromJson(messageJson.toString(), FbPostBack.class);
                facebookBot.handlePostBack(postBack);
            } else {
                FbMessage message = new Gson().fromJson(messageJson.toString(), FbMessage.class);
                facebookBot.handleMessage(message);
            }
        } catch (Exception e) {
            facebookLog.error(e);
        }
    }

    @RequestMapping(value = "/viber", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    public void getViberUpdates(@RequestBody(required = false) String body) {
        JSONObject messageJson = new JSONObject(body);
        viberLog.info("Received a message from viber: " + messageJson.toString());
        try {
            if (messageJson.getString("event").equals("conversation_started")) {
                JSONObject senderJson = messageJson.getJSONObject("user");
                Sender sender = new Gson().fromJson(senderJson.toString(), Sender.class);
                viberBot.handleSubscribe(sender);
            }
            if (messageJson.getString("event").equals("message")) {
                VbMessage message = new Gson().fromJson(body, VbMessage.class);
                viberBot.handleMessage(message);
            }
        } catch (Exception e) {
            viberLog.info(e);
        }
    }

    @PreDestroy
    public void tearDown() {
        monitorLog.info("Stopping LNServer..");
        lnServer.interrupt();

        monitorLog.info("Stopping LNClient..");
        lnClient.interrupt();

        monitorLog.info("Stopping shki monitor..");
        ShkiMonitor.getInstance().interrupt();
    }
}
