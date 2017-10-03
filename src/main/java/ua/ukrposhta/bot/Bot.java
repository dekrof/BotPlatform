package ua.ukrposhta.bot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ua.ukrposhta.models.PackageStatus;
import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.readers.ButtonPayloadsReader;
import ua.ukrposhta.utils.readers.MessagesReader;
import ua.ukrposhta.utils.readers.PicturesReader;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public abstract class Bot {
    protected Properties properties = new Properties();
    BotLogger telegramLog = BotLogger.getLogger(LoggerType.TELEGRAM);
    BotLogger facebookLog = BotLogger.getLogger(LoggerType.FACEBOOK);
    BotLogger viberLog = BotLogger.getLogger(LoggerType.VIBER);
    protected Set<String> finalStatuses;

    protected MessagesReader messagesReader;
    protected ButtonPayloadsReader buttonPayloadsReader;
    protected PicturesReader picturesReader;

    Bot() {
        try {
            telegramLog.info("Loading bot properties");
            facebookLog.info("Loading bot properties");
            properties.load(this.getClass().getClassLoader().getResourceAsStream("properties/application.properties"));
            String appProfile = properties.getProperty("app.profile") + ".";
            System.setProperty("tracking.baseUrl", properties.getProperty("tracking.baseUrl"));
            System.setProperty("package.finalStatuses", properties.getProperty("package.finalStatuses"));

            System.setProperty("telegramBot.baseUrl", properties.getProperty("telegramBot.baseUrl"));
            System.setProperty("telegramBot.botUsername", properties.getProperty(appProfile + "telegramBot.botUsername"));
            System.setProperty("telegramBot.botToken", properties.getProperty(appProfile + "telegramBot.botToken"));
            System.setProperty("telegramBot.oldBotToken", properties.getProperty(appProfile + "telegramBot.oldBotToken"));
            System.setProperty("facebookBot.baseUrl", properties.getProperty("facebookBot.baseUrl"));
            System.setProperty("facebookBot.pageToken", properties.getProperty(appProfile + "facebookBot.pageToken"));
            System.setProperty("facebookBot.validationToken", properties.getProperty(appProfile + "facebookBot.validationToken"));
            System.setProperty("viberBot.baseUrl", properties.getProperty("viberBot.baseUrl"));
            System.setProperty("viberBot.botToken", properties.getProperty(appProfile + "viberBot.botToken"));

            System.setProperty("monitor.port", properties.getProperty(appProfile + "monitor.port"));
            System.setProperty("monitor.instances", properties.getProperty(appProfile + "monitor.instances"));
            System.setProperty("monitor.LNScan.period", properties.getProperty("monitor.LNScan.period"));
            finalStatuses = new HashSet<>(Arrays.asList(properties.getProperty("package.finalStatuses").split(",")));
            telegramLog.info("Bot properties loaded");
            facebookLog.info("Bot properties loaded");

            messagesReader = MessagesReader.getInstance();
            buttonPayloadsReader = ButtonPayloadsReader.getInstance();
            picturesReader = PicturesReader.getInstance();
        } catch (IOException | JAXBException e) {
            telegramLog.error(e);
            facebookLog.error(e);
        }
    }

    public PackageStatus requestBarcodeStatus(String barcode, String userId, BotLogger log) {
        List<PackageStatus> packageStatuses = requestAllStatuses(barcode, log);
        PackageStatus packageStatus = null;
        if (packageStatuses != null && packageStatuses.size() > 0) {
            packageStatus = packageStatuses.get(packageStatuses.size() - 1);
        }

        return packageStatus;
    }

    public PackageStatus requestBarcodePreviousStatus(String barcode, BotLogger log) {
        List<PackageStatus> packageStatuses = requestAllStatuses(barcode, log);
        PackageStatus packageStatus = null;
        if (packageStatuses != null && packageStatuses.size() > 1) {
            packageStatus = packageStatuses.get(packageStatuses.size() - 2);
        }

        return packageStatus;
    }

    protected List<PackageStatus> requestAllStatuses(String barcode, BotLogger log) {
        List<PackageStatus> packageStatuses = null;
        log.info("Requesting barcode " + barcode + " statuses.");
        try {
            String baseUrl = System.getProperty("tracking.baseUrl");
            String finalUrl = baseUrl
                    + (baseUrl.endsWith("/") ? "" : "/")
                    + "StatusTracking/statuses?barcode="
                    + barcode;
            URL url = new URL(finalUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            Gson gson = new Gson();
            packageStatuses = gson.fromJson(response.toString(), new TypeToken<LinkedList<PackageStatus>>(){}.getType());
        } catch (IOException e) {
            log.error("Error during barcode(" + barcode + ") statuses request.");
            log.error(e);
        }
        return packageStatuses;
    }

    public String createStatusResponseMessage(PackageStatus packageStatus, String barcode) throws Exception {
        String defaultText = messagesReader.getMessage("barcode not found").trim();
        String responseText = String.format(defaultText, barcode);
        if (packageStatus != null) {
            defaultText = messagesReader.getMessage("barcode found").trim();
            responseText = String.format(defaultText,
                    packageStatus.getEventName(),
                    packageStatus.getIndex(),
                    (packageStatus.getName() == null ? "" : packageStatus.getName()),
                    packageStatus.getCountry());
        }
        return responseText;
    }
}
