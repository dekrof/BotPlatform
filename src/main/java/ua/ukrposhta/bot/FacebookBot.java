package ua.ukrposhta.bot;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import ua.ukrposhta.dao.FacebookUserDao;
import ua.ukrposhta.dao.MonitorDao;
import ua.ukrposhta.dao.UnknownMessageDao;
import ua.ukrposhta.dao.VPZIndexDao;
import ua.ukrposhta.entities.FacebookUser;
import ua.ukrposhta.entities.VPZIndex;
import ua.ukrposhta.entities.WorkSchedule;
import ua.ukrposhta.models.PackageStatus;
import ua.ukrposhta.models.facebook.FacebookUserProfile;
import ua.ukrposhta.models.facebook.FbIncomingUpdate;
import ua.ukrposhta.models.facebook.FbMessage;
import ua.ukrposhta.models.facebook.FbPostBack;
import ua.ukrposhta.models.facebook.buttons.ButtonTemplate;
import ua.ukrposhta.models.facebook.template.GenericTemplate;
import ua.ukrposhta.models.facebook.template.PayloadElement;
import ua.ukrposhta.models.texts.ButtonPayload;
import ua.ukrposhta.utils.senders.FacebookSender;
import ua.ukrposhta.utils.types.BotType;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FacebookBot extends Bot {
    private static FacebookBot instance;
    private static FacebookSender sender;
    private FacebookUserDao facebookUserDao = new FacebookUserDao();
    private MonitorDao monitorDao = new MonitorDao();

    private FacebookBot() {
        sender = FacebookSender.getInstance();
        try {
            String news = messagesReader.getMessage("news");
            if (!news.isEmpty() || news.length() > 0) {
                sendNews(news);
            }
        } catch (Exception e) {
            facebookLog.error(e);
        }
    }

    public static FacebookBot getInstance() {
        if (instance == null) {
            instance = new FacebookBot();
        }
        return instance;
    }

    private FacebookUserProfile getUserProfile(String userId) {
        FacebookUserProfile profile = null;
        String pageToken = System.getProperty("facebookBot.pageToken");
        String finalUrl = "https://graph.facebook.com/v2.7/"
                + userId
                + "?fields=first_name,last_name,profile_pic,locale,timezone,gender&access_token="
                + pageToken;

        facebookLog.info("Requesting user " + userId + " profile information.");
        try {
            URL url = new URL(finalUrl);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setRequestMethod("GET");

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
                facebookLog.info("Response for the profile request: " + sb.toString());
                profile = new Gson().fromJson(sb.toString(), FacebookUserProfile.class);
            } else {
                facebookLog.warn("Response for the profile request: " + httpsURLConnection.getResponseMessage());
            }
        } catch (IOException e) {
            facebookLog.info(e);
        }
        return profile;
    }

    public void handlePostBack(FbPostBack postBack) throws Exception {
        if (!postBack.getPostBack().getText().isEmpty()) {
            String userId = postBack.getSender().getId();
            facebookUserDao.addNewUser(userId, getUserProfile(userId));
            switch (postBack.getPostBack().getText().toLowerCase()) {
                case "start": {
                    String text = messagesReader.getMessage("start").trim();
                    ButtonPayload trackingButtonPayload = buttonPayloadsReader.getButtonPayload("menu tracking");
                    ButtonPayload officesButtonPayload = buttonPayloadsReader.getButtonPayload("menu offices");
                    ButtonPayload tariffsButtonPayload = buttonPayloadsReader.getButtonPayload("menu tariffs");
                    ButtonPayload helpButtonPayload = buttonPayloadsReader.getButtonPayload("menu help");

                    sender.setRecipientId(postBack.getSender().getId())
                            .addQuickReply(trackingButtonPayload.getCaption(), trackingButtonPayload.getCaption())
                            .addQuickReply(officesButtonPayload.getCaption(), officesButtonPayload.getCaption())
                            .addQuickReply(tariffsButtonPayload.getCaption(), tariffsButtonPayload.getCaption())
                            .addQuickReply(helpButtonPayload.getCaption(), helpButtonPayload.getCaption())
                            .setText(text)
                            .sendMessage();
                    FacebookUserProfile userProfile = getUserProfile(postBack.getSender().getId());
                    facebookUserDao.addNewUser(postBack.getSender().getId(), userProfile);
                    break;
                }
                case "трекінг": {
                    handleTrackingHelp(postBack);
                    break;
                }
                case "відділення": {
                    handlePostalCodeHelp(postBack);
                    break;
                }
                case "допомога": {
                    handleHelpRequest(postBack);
                    break;
                }
                case "тарифи": {
                    handleTariffsRequest(postBack);
                    break;
                }
                case "стандарт": {
                    handleStandardTariffRequest(postBack);
                    break;
                }
                case "експрес": {
                    handleExpressTariffRequest(postBack);
                    break;
                }
                case "smartbox": {
                    handleSmartBoxTariffRequest(postBack);
                    break;
                }
                case "універсальні": {
                    handleUniversalTariffRequest(postBack);
                    break;
                }
                case "міжнародні": {
                    handleInternationalTariffRequest(postBack);
                    break;
                }
                default: {

                }
            }
        }
    }

    public void handleMessage(FbMessage message) {
        String userId = message.getSender().getId();
        if (message.getStickerId() != null && message.getStickerId().equals("369239263222822")) {
            sender.setRecipientId(userId)
                    .setText("\uD83D\uDE0A\nДякую! Дуже приємно!")
                    .sendMessage();
            return;
        }
        if (!message.getText().isEmpty()) {
            facebookUserDao.addNewUser(userId, getUserProfile(userId));
            try {
                switch (message.getText().toLowerCase()) {
                    case "меню":
                    case "головне меню": {
                        handleMainMenuRequest(message);
                        break;
                    }
                    case "трекінг": {
                        handleTrackingHelp(message);
                        break;
                    }
                    case "продовжити пошук":
                    case "відділення": {
                        handlePostalCodeHelp(message);
                        break;
                    }
                    case "допомога": {
                        handleHelpRequest(message);
                        break;
                    }
                    case "тарифи": {
                        handleTariffsRequest(message);
                        break;
                    }

                    case "стандарт тариф":
                    case "тариф стандарт":
                    case "стандарт тарифи":
                    case "стандарт": {
                        handleStandardTariffRequest(message);
                        break;
                    }
                    case "експрес тариф":
                    case "тариф експрес":
                    case "експрес тарифи":
                    case "експрес": {
                        handleExpressTariffRequest(message);
                        break;
                    }
                    case "smartbox тариф":
                    case "тариф smartbox":
                    case "smartbox тарифи":
                    case "смартбокс":
                    case "смарт бокс":
                    case "smartbox": {
                        handleSmartBoxTariffRequest(message);
                        break;
                    }
                    case "тарифи універсальні":
                    case "універсальний тариф":
                    case "тариф універсальний":
                    case "універсальні тарифи":
                    case "універсальні": {
                        handleUniversalTariffRequest(message);
                        break;
                    }
                    case "міжнародний":
                    case "міжнародний тариф":
                    case "тариф міжнародний":
                    case "тарифи міжнародні":
                    case "міжнародні тарифи":
                    case "міжнародні": {
                        handleInternationalTariffRequest(message);
                        break;
                    }
                    case "відстежувати": {
                        startTracking(message);
                        break;
                    }
                    case "відписатися": {
                        stopTracking(message);
                        break;
                    }
                    case "спасибо":
                    case "дяк":
                    case "дякую":
                    case "дяки":
                    case "щиро дякую":
                    case "спс":
                    case "спасибки":
                    case "спасиб":
                    case "пасиб":
                    case "пасиба":
                    case "благодарю": {
                        String text = messagesReader.getMessage("thank reply").trim();
                        sender.setRecipientId(message.getSender().getId())
                                .setText(text)
                                .sendMessage();
                        break;
                    }
                    /* Examples:
                    case "template": {
                        ButtonTemplate buttonTemplate = new ButtonTemplate("Text message here..")
                                .addUrlButton("Visit google page", "http://google.com");
                        sender.setRecipientId(message.getSender().getId())
                                .addButtonTemplate(buttonTemplate)
                                .sendMessage();
                        break;
                    }
                    case "quick": {
                        sender.setRecipientId(message.getSender().getId())
                                .setText("Message received: '" + message.getText() + "'")
                                .addQuickReply("Button 1", "Button 1 payload")
                                .addQuickReply("Button 2", "Button 2 payload")
                                .sendMessage();
                        break;
                    }
                    case "button 2":
                    case "button 1": {
                        sender.setRecipientId(message.getSender().getId())
                                .setText("Quick reply payload: " + message.getQuickReply().getText())
                                .sendMessage();
                        break;
                    }
                    */
                    default: {
                        Pattern barcodePattern = Pattern.compile("([a-zA-Z]{2}\\d{9}[a-zA-Z]{2})|(\\d{13})");
                        Pattern postIndexPattern = Pattern.compile("\\d{5}");

                        Matcher matcher = barcodePattern.matcher(message.getText());
                        if (matcher.matches()) {
                            handleBarcodeRequest(message);
                            return;
                        }

                        matcher = postIndexPattern.matcher(message.getText());
                        if (matcher.matches()) {
                            handlePostalCodeRequest(message);
                            return;
                        }

                        String text = messagesReader.getMessage("unrecognized message").trim();
                        sender.setRecipientId(message.getSender().getId())
                                .setText(text)
                                .sendMessage();

                        new UnknownMessageDao().addMessage(message.getSender().getId(), message.getText(), text, BotType.FACEBOOK);
                    }
                }
            } catch (Exception e) {
                facebookLog.error(e);
            }
        }
    }

    public void sendBarcodeUpdate(String userId, PackageStatus prevStatus, PackageStatus newStatus) throws Exception {
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
        String defaultText = messagesReader.getMessage("track update").trim();
        String text = String.format(defaultText, newStatus.getBarcode(),
                prevStatus.getEventName(), prevStatus.getName(), prevStatus.getIndex(),
                newStatus.getEventName(), newStatus.getName(), newStatus.getIndex());

        sender.setRecipientId(userId)
                .setText(text)
                .addQuickReply(buttonPayload.getCaption(), String.format(buttonPayload.getCallback(), newStatus.getBarcode()))
                .sendMessage();
    }

    private void sendNews(String news) {
        List<FacebookUser> users = facebookUserDao.getAllUsers();
        users.forEach(user -> sender.setRecipientId(user.getUserId()).setText(news).sendMessage());
    }

// *******************************************************************************************************************
//
// Inner handle methods which are called from handleMessage or handlePostBack
//
// *******************************************************************************************************************

    private void handleMainMenuRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload trackingButtonPayload = buttonPayloadsReader.getButtonPayload("menu tracking");
        ButtonPayload officesButtonPayload = buttonPayloadsReader.getButtonPayload("menu offices");
        ButtonPayload tariffsButtonPayload = buttonPayloadsReader.getButtonPayload("menu tariffs");
        ButtonPayload helpButtonPayload = buttonPayloadsReader.getButtonPayload("menu help");

        sender.setRecipientId(chatId)
                .addQuickReply(trackingButtonPayload.getCaption(), trackingButtonPayload.getCaption())
                .addQuickReply(officesButtonPayload.getCaption(), officesButtonPayload.getCaption())
                .addQuickReply(tariffsButtonPayload.getCaption(), tariffsButtonPayload.getCaption())
                .addQuickReply(helpButtonPayload.getCaption(), helpButtonPayload.getCaption())
                .setText("Головне меню")
                .sendMessage();
    }

    private void handleTrackingHelp(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        String text = messagesReader.getMessage("tracking help").trim();
        sender.setRecipientId(chatId)
                .setText(text)
                .sendMessage();
    }

    private void handlePostalCodeHelp(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        String text = messagesReader.getMessage("postal code help").trim();
        sender.setRecipientId(chatId)
                .setText(text)
                .sendMessage();
    }

    private void handleHelpRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();

        String text = messagesReader.getMessage("help").trim();
        ButtonPayload openSiteButtonPayload = buttonPayloadsReader.getButtonPayload("open site");
        ButtonTemplate buttonTemplate = new ButtonTemplate(text)
                .addUrlButton(openSiteButtonPayload.getCaption(), openSiteButtonPayload.getUrl());
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        sender.setRecipientId(chatId)
                .addButtonTemplate(buttonTemplate)
                .addQuickReply(mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption())
                .sendMessage();
    }

    private void handleTariffsRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");

        String text = messagesReader.getMessage("tariffs request").trim();
        sender.setRecipientId(chatId)
                .setText(text)
                .addQuickReply(expressTariffPayload.getCaption(), expressTariffPayload.getCallback())
                .addQuickReply(standardTariffPayload.getCaption(), standardTariffPayload.getCallback())
                .addQuickReply(internationalPayload.getCaption(), internationalPayload.getCallback())
                .addQuickReply(smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback())
                .addQuickReply(universalTariffPayload.getCaption(), universalTariffPayload.getCallback())
                .sendMessage();
    }

    private void handleStandardTariffRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("standard detail");
        String picture = picturesReader.getPictureUrl("standard tariff");

        sender.setRecipientId(chatId)
                .sendPicture(picture);
        ButtonTemplate buttonTemplate = new ButtonTemplate(buttonPayload.getDescription())
                .addUrlButton(buttonPayload.getCaption(), buttonPayload.getUrl());
        sender.setRecipientId(chatId)
                .addButtonTemplate(buttonTemplate)
                .sendMessage();

        String text = messagesReader.getMessage("other tariffs").trim();
        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        sender.setRecipientId(chatId)
                .setText(text)
                .addQuickReply(expressTariffPayload.getCaption(), expressTariffPayload.getCallback())
                .addQuickReply(internationalPayload.getCaption(), internationalPayload.getCallback())
                .addQuickReply(smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback())
                .addQuickReply(universalTariffPayload.getCaption(), universalTariffPayload.getCallback())
                .sendMessage();
    }

    private void handleExpressTariffRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("express detail");
        String picture = picturesReader.getPictureUrl("express tariff");

        sender.setRecipientId(chatId)
                .sendPicture(picture);
        ButtonTemplate buttonTemplate = new ButtonTemplate(buttonPayload.getDescription())
                .addUrlButton(buttonPayload.getCaption(), buttonPayload.getUrl());
        sender.setRecipientId(chatId)
                .addButtonTemplate(buttonTemplate)
                .sendMessage();

        String text = messagesReader.getMessage("other tariffs").trim();
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        sender.setRecipientId(chatId)
                .setText(text)
                .addQuickReply(standardTariffPayload.getCaption(), standardTariffPayload.getCallback())
                .addQuickReply(internationalPayload.getCaption(), internationalPayload.getCallback())
                .addQuickReply(smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback())
                .addQuickReply(universalTariffPayload.getCaption(), universalTariffPayload.getCallback())
                .sendMessage();
    }

    private void handleSmartBoxTariffRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("smartBox detail");
        String picture = picturesReader.getPictureUrl("smartBox tariff");

        sender.setRecipientId(chatId)
                .sendPicture(picture);
        ButtonTemplate buttonTemplate = new ButtonTemplate(buttonPayload.getDescription())
                .addUrlButton(buttonPayload.getCaption(), buttonPayload.getUrl());
        sender.setRecipientId(chatId)
                .addButtonTemplate(buttonTemplate)
                .sendMessage();

        String text = messagesReader.getMessage("other tariffs").trim();
        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        sender.setRecipientId(chatId)
                .setText(text)
                .addQuickReply(expressTariffPayload.getCaption(), expressTariffPayload.getCallback())
                .addQuickReply(standardTariffPayload.getCaption(), standardTariffPayload.getCallback())
                .addQuickReply(internationalPayload.getCaption(), internationalPayload.getCallback())
                .addQuickReply(universalTariffPayload.getCaption(), universalTariffPayload.getCallback())
                .sendMessage();
    }

    private void handleUniversalTariffRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("universal detail");

        ButtonTemplate buttonTemplate = new ButtonTemplate(buttonPayload.getDescription())
                .addUrlButton(buttonPayload.getCaption(), buttonPayload.getUrl());
        sender.setRecipientId(chatId)
                .addButtonTemplate(buttonTemplate)
                .sendMessage();

        String text = messagesReader.getMessage("other tariffs").trim();
        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        sender.setRecipientId(chatId)
                .setText(text)
                .addQuickReply(expressTariffPayload.getCaption(), expressTariffPayload.getCallback())
                .addQuickReply(standardTariffPayload.getCaption(), standardTariffPayload.getCallback())
                .addQuickReply(internationalPayload.getCaption(), internationalPayload.getCallback())
                .addQuickReply(smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback())
                .sendMessage();
    }

    private void handleInternationalTariffRequest(FbIncomingUpdate message) throws Exception {
        String chatId = message.getSender().getId();
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("international detail");

        ButtonTemplate buttonTemplate = new ButtonTemplate(buttonPayload.getDescription())
                .addUrlButton(buttonPayload.getCaption(), buttonPayload.getUrl());
        sender.setRecipientId(chatId)
                .addButtonTemplate(buttonTemplate)
                .sendMessage();

        String text = messagesReader.getMessage("other tariffs").trim();
        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        sender.setRecipientId(chatId)
                .setText(text)
                .addQuickReply(expressTariffPayload.getCaption(), expressTariffPayload.getCallback())
                .addQuickReply(standardTariffPayload.getCaption(), standardTariffPayload.getCallback())
                .addQuickReply(smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback())
                .addQuickReply(universalTariffPayload.getCaption(), universalTariffPayload.getCallback())
                .sendMessage();
    }

    private void handleBarcodeRequest(FbMessage message) throws Exception {
        String chatId = message.getSender().getId();
        PackageStatus status = requestBarcodeStatus(message.getText(), message.getSender().getId(), facebookLog);
        String responseText = createStatusResponseMessage(status, message.getText());

        if (status != null) {
            ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("barcode details");
            ButtonTemplate buttonTemplate = new ButtonTemplate(responseText)
                    .addUrlButton(buttonPayload.getCaption(), buttonPayload.getUrl());
            sender.setRecipientId(message.getSender().getId())
                    .addButtonTemplate(buttonTemplate)
                    .sendMessage();
            if (!finalStatuses.contains(status.getEvent())) {
                if (monitorDao.getStatus(message.getText().toUpperCase(), chatId).isEmpty()) {
                    responseText = messagesReader.getMessage("track start offer").trim();
                    buttonPayload = buttonPayloadsReader.getButtonPayload("track start");
                } else {
                    responseText = messagesReader.getMessage("track stop offer").trim();
                    buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
                }
                ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
                sender.setRecipientId(message.getSender().getId())
                        .setText(responseText)
                        .addQuickReply(buttonPayload.getCaption(), String.format(buttonPayload.getCallback(), message.getText()))
                        .addQuickReply(mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption())
                        .sendMessage();
            }
        } else {
            sender.setRecipientId(message.getSender().getId())
                    .setText(responseText)
                    .sendMessage();
        }
    }

    private void handlePostalCodeRequest(FbMessage message) throws Exception {
        String postIndex = message.getText();
        VPZIndex vpzIndex = new VPZIndexDao().getVPZIndex(postIndex, telegramLog);
        String chatId = message.getSender().getId();

        String responseText;
        if (vpzIndex == null) {
            String templateResponse = messagesReader.getMessage("office not found").trim();
            responseText = String.format(templateResponse, postIndex);
            ButtonPayload continueSearchButtonPayload = buttonPayloadsReader.getButtonPayload("continue search");
            ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

            sender.setRecipientId(chatId)
                    .setText(responseText)
                    .addQuickReply(continueSearchButtonPayload.getCaption(), continueSearchButtonPayload.getCallback())
                    .addQuickReply(mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption())
                    .sendMessage();
        } else {
            String templateResponse = messagesReader.getMessage("office found").trim();
            responseText = String.format(templateResponse, vpzIndex.getOblastName(), vpzIndex.getCityName(), vpzIndex.getPostFilial().getAddress()) + "\n";
            for (WorkSchedule workSchedule : vpzIndex.getPostFilial().getWorkScheduleList()) {
                responseText = responseText
                        + workSchedule.getShortDays()
                        + ", " + String.copyValueOf(workSchedule.getWorkFrom().toCharArray(), 11, 5)
                        + "-" + String.copyValueOf(workSchedule.getWorkTo().toCharArray(), 11, 5)
                        + "\n";
            }

            sender.setRecipientId(chatId)
                    .setText(responseText)
                    .sendMessage();

            try {
                String imageUrl = "https://maps.googleapis.com/maps/api/staticmap?size=764x400&center="
                        + vpzIndex.getPostFilialGeo().getLatitude() + "," + vpzIndex.getPostFilialGeo().getLongitude()
                        + "&markers=" + vpzIndex.getPostFilialGeo().getLatitude() + ","
                        + vpzIndex.getPostFilialGeo().getLongitude();
                String itemUrl = "https://maps.google.com/maps?q=" + vpzIndex.getPostFilialGeo().getLatitude() + "," + vpzIndex.getPostFilialGeo().getLongitude();
                PayloadElement payloadElement = new PayloadElement()
                        .setTitle("Відділення " + postIndex)
                        .setItemUrl(itemUrl)
                        .setImageUrl(imageUrl);
                GenericTemplate genericTemplate = new GenericTemplate(payloadElement);
                ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
                sender.setRecipientId(chatId)
                        .addQuickReply(mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption())
                        .addGenericTemplate(genericTemplate)
                        .sendLocation();
            } catch (NullPointerException npe) {
                facebookLog.error("Coordinates not found for " + postIndex + ". Unable to send location to user.");
            }
        }
    }

    private void startTracking(FbMessage message) throws Exception {
        String[] callbackMessage = message.getQuickReply().getText().split(" ");
        String chatId = message.getSender().getId();

        PackageStatus currentStatus = requestBarcodeStatus(callbackMessage[1].toUpperCase(),
                chatId, facebookLog);
        String status = currentStatus == null ? "не знайдене" : currentStatus.getEventName();
        monitorDao.startTracking(callbackMessage[1].toUpperCase(), chatId, status, BotType.FACEBOOK);

        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
        String defaultText = messagesReader.getMessage("track started").trim();
        String text = String.format(defaultText, callbackMessage[1]);
        sender.setRecipientId(chatId)
                .addQuickReply(buttonPayload.getCaption(), String.format(buttonPayload.getCallback(), callbackMessage[1].toUpperCase()))
                .setText(text)
                .sendMessage();
    }

    private void stopTracking(FbMessage message) throws Exception {
        String[] callbackMessage = message.getQuickReply().getText().split(" ");
        String chatId = message.getSender().getId();

        monitorDao.stopTracking(callbackMessage[1].toUpperCase(), chatId, BotType.FACEBOOK);
        String defaultText = messagesReader.getMessage("track stopped").trim();
        String text = String.format(defaultText, callbackMessage[1]);
        sender.setRecipientId(chatId)
                .setText(text)
                .sendMessage();
    }
}
