package ua.ukrposhta.bot;

import ua.ukrposhta.dao.MonitorDao;
import ua.ukrposhta.dao.UnknownMessageDao;
import ua.ukrposhta.dao.VPZIndexDao;
import ua.ukrposhta.dao.ViberUserDao;
import ua.ukrposhta.entities.VPZIndex;
import ua.ukrposhta.entities.WorkSchedule;
import ua.ukrposhta.models.PackageStatus;
import ua.ukrposhta.models.texts.ButtonPayload;
import ua.ukrposhta.models.viber.Sender;
import ua.ukrposhta.models.viber.VbMessage;
import ua.ukrposhta.models.viber.keyboard.Keyboard;
import ua.ukrposhta.models.viber.keyboard.ReplyButton;
import ua.ukrposhta.models.viber.keyboard.UrlButton;
import ua.ukrposhta.utils.senders.ViberSender;
import ua.ukrposhta.utils.types.BotType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViberBot extends Bot {
    private ViberUserDao viberUserDao = new ViberUserDao();
    private MonitorDao monitorDao = new MonitorDao();

    private ViberSender sender;

    private static ViberBot instance;

    private ViberBot() {
        sender = ViberSender.getInstance();
    }

    public static ViberBot getInstance() {
        if (instance == null) {
            instance = new ViberBot();
        }
        return instance;
    }

    private Keyboard getMainKeyboard() {
        Keyboard mainMenuKeyboard = null;
        try {
            ButtonPayload trackingButtonPayload = buttonPayloadsReader.getButtonPayload("menu tracking");
            ButtonPayload officesButtonPayload = buttonPayloadsReader.getButtonPayload("menu offices");
            ButtonPayload tariffsButtonPayload = buttonPayloadsReader.getButtonPayload("menu tariffs");
            ButtonPayload helpButtonPayload = buttonPayloadsReader.getButtonPayload("menu help");

            mainMenuKeyboard = new Keyboard()
                    .addButton(new ReplyButton(3, 1, trackingButtonPayload.getCaption(), trackingButtonPayload.getCaption()))
                    .addButton(new ReplyButton(3, 1, officesButtonPayload.getCaption(), officesButtonPayload.getCaption()))
                    .addButton(new ReplyButton(3, 1, tariffsButtonPayload.getCaption(), tariffsButtonPayload.getCaption()))
                    .addButton(new ReplyButton(3, 1, helpButtonPayload.getCaption(), helpButtonPayload.getCaption()));
        } catch (Exception e) {
            viberLog.error(e);
        }

        return mainMenuKeyboard;
    }

    public void handleSubscribe(Sender newUser) throws Exception {
        viberUserDao.addNewUser(newUser);
        String message = messagesReader.getMessage("start");

        sender.setReceiver(newUser.getId())
                .setKeyboard(getMainKeyboard())
                .setText(message)
                .sendMessage();
    }

    public void handleMessage(VbMessage vbMessage) {
        String receiverId = vbMessage.getSender().getId();

        if (!vbMessage.getMessage().getText().isEmpty()) {
            try {
                if (vbMessage.getMessage().getText().toLowerCase().startsWith("відстежувати")) {
                    startTracking(vbMessage);
                    return;
                }
                if (vbMessage.getMessage().getText().toLowerCase().startsWith("припинити")) {
                    stopTracking(vbMessage);
                    return;
                }
                switch (vbMessage.getMessage().getText().toLowerCase()) {
                    case "головне меню": {
                        handleMainMenuRequest(vbMessage);
                        break;
                    }
                    case "трекінг": {
                        handleTrackingHelp(vbMessage);
                        break;
                    }
                    case "відділення": {
                        handlePostalCodeHelp(vbMessage);
                        break;
                    }
                    case "тарифи": {
                        handleTariffsRequest(vbMessage);
                        break;
                    }
                    case "стандарт тариф":
                    case "тариф стандарт":
                    case "стандарт тарифи":
                    case "стандарт": {
                        handleStandardTariffRequest(vbMessage);
                        break;
                    }
                    case "експрес тариф":
                    case "тариф експрес":
                    case "експрес тарифи":
                    case "експрес": {
                        handleExpressTariffRequest(vbMessage);
                        break;
                    }
                    case "smartbox тариф":
                    case "тариф smartbox":
                    case "smartbox тарифи":
                    case "смартбокс":
                    case "смарт бокс":
                    case "smart box":
                    case "smartbox": {
                        handleSmartBoxTariffRequest(vbMessage);
                        break;
                    }
                    case "тарифи універсальні":
                    case "універсальний тариф":
                    case "тариф універсальний":
                    case "універсальні тарифи":
                    case "універсальні": {
                        handleUniversalTariffRequest(vbMessage);
                        break;
                    }
                    case "міжнародний":
                    case "міжнародний тариф":
                    case "тариф міжнародний":
                    case "тарифи міжнародні":
                    case "міжнародні тарифи":
                    case "міжнародні": {
                        handleInternationalTariffRequest(vbMessage);
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
                        sender.setReceiver(receiverId)
                                .setText(text)
                                .setKeyboard(getMainKeyboard())
                                .sendMessage();
                        break;
                    }
                    case "допомога": {
                        handleHelpRequest(vbMessage);
                        break;
                    }
                    default: {
                        Pattern barcodePattern = Pattern.compile("([a-zA-Z]{2}\\d{9}[a-zA-Z]{2})|(\\d{13})");
                        Pattern postIndexPattern = Pattern.compile("\\d{5}");
                        Pattern skipHttpPattern = Pattern.compile("^http.+");

                        Matcher matcher = barcodePattern.matcher(vbMessage.getMessage().getText());
                        if (matcher.matches()) {
                            handleBarcodeRequest(vbMessage);
                            return;
                        }

                        matcher = postIndexPattern.matcher(vbMessage.getMessage().getText());
                        if (matcher.matches()) {
                            handlePostalCodeRequest(vbMessage);
                            return;
                        }

                        matcher = skipHttpPattern.matcher(vbMessage.getMessage().getText());
                        if (matcher.matches()) {
                            return;
                        }

                        String text = messagesReader.getMessage("unrecognized message").trim();
                        ButtonPayload helpButtonPayload = buttonPayloadsReader.getButtonPayload("menu help");
                        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
                        sender.setReceiver(receiverId)
                                .setText(text)
                                .addButton(new ReplyButton(3, 1, helpButtonPayload.getCaption(), helpButtonPayload.getCaption()))
                                .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                                .sendMessage();

                        new UnknownMessageDao().addMessage(receiverId, vbMessage.getMessage().getText(), text, BotType.VIBER);
                    }
                }
            } catch (Exception e) {
                viberLog.error(e);
            }
        }
    }

    public void sendBarcodeUpdate(String userId, PackageStatus newStatus, PackageStatus prevStatus) throws Exception {
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
        String defaultText = messagesReader.getMessage("track update").trim();
        String text = String.format(defaultText, newStatus.getBarcode(),
                prevStatus.getEventName(), prevStatus.getName(), prevStatus.getIndex(),
                newStatus.getEventName(), newStatus.getName(), newStatus.getIndex());

        sender.setReceiver(userId)
                .addButton(new ReplyButton(3, 1, buttonPayload.getCaption(), String.format(buttonPayload.getCallback(), newStatus.getBarcode())))
                .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .setText(text)
                .sendMessage();
    }

// *******************************************************************************************************************
//
// Inner handle methods which are called from handleMessage or handlePostBack
//
// *******************************************************************************************************************

    private void handleMainMenuRequest(VbMessage message) {
        String receiverId = message.getSender().getId();

        sender.setReceiver(receiverId)
                .setKeyboard(getMainKeyboard())
                .sendMessage();
    }

    private void handleHelpRequest(VbMessage message) throws Exception {
        String receiverId = message.getSender().getId();

        String text = messagesReader.getMessage("help").trim();
        ButtonPayload openSiteButtonPayload = buttonPayloadsReader.getButtonPayload("open site");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
        Keyboard keyboard = new Keyboard()
                .addButton(new UrlButton(3, 1, openSiteButtonPayload.getCaption(), openSiteButtonPayload.getUrl()))
                .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()));
        sender.setReceiver(receiverId)
                .setText(text)
                .setKeyboard(keyboard)
                .sendMessage();
    }

    private void handleTrackingHelp(VbMessage message) throws Exception {
        String receiverId = message.getSender().getId();

        String text = messagesReader.getMessage("tracking help").trim();
        sender.setReceiver(receiverId)
                .setText(text)
                .sendMessage();
    }

    private void handlePostalCodeHelp(VbMessage message) throws Exception {
        String receiverId = message.getSender().getId();

        String text = messagesReader.getMessage("postal code help").trim();
        sender.setReceiver(receiverId)
                .setText(text)
                .sendMessage();
    }

    public void handleTariffsRequest(VbMessage message) throws Exception {
        String receiverId = message.getSender().getId();

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        String text = messagesReader.getMessage("tariffs request").trim();
        sender.setReceiver(receiverId)
                .addButton(new ReplyButton(3, 1, expressTariffPayload.getCaption(), expressTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, standardTariffPayload.getCaption(), standardTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, internationalPayload.getCaption(), internationalPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, universalTariffPayload.getCaption(), universalTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .setText(text)
                .sendMessage();
    }

    private void handleStandardTariffRequest(VbMessage message) throws Exception {
        String chatId = message.getSender().getId();

        ButtonPayload tariffDetailsPayload = buttonPayloadsReader.getButtonPayload("standard detail");
        String picture = picturesReader.getPictureUrl("standard tariff");

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        sender.setReceiver(chatId)
                .setPicture(picture)
                .setText(standardTariffPayload.getCaption() + ":\n" + tariffDetailsPayload.getUrl())
                .sendPicture();
        sender.setReceiver(chatId)
                .addButton(new ReplyButton(3, 1, expressTariffPayload.getCaption(), expressTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, internationalPayload.getCaption(), internationalPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, universalTariffPayload.getCaption(), universalTariffPayload.getCallback()))
                .addButton(new ReplyButton(6, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .sendMessage();
    }

    private void handleExpressTariffRequest(VbMessage message) throws Exception {
        String chatId = message.getSender().getId();

        ButtonPayload tariffDetailsPayload = buttonPayloadsReader.getButtonPayload("express detail");
        String picture = picturesReader.getPictureUrl("express tariff");

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        sender.setReceiver(chatId)
                .setPicture(picture)
                .setText(expressTariffPayload.getCaption() + ":\n" + tariffDetailsPayload.getUrl())
                .sendPicture();
        sender.setReceiver(chatId)
                .addButton(new ReplyButton(3, 1, standardTariffPayload.getCaption(), standardTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, internationalPayload.getCaption(), internationalPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, universalTariffPayload.getCaption(), universalTariffPayload.getCallback()))
                .addButton(new ReplyButton(6, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .sendMessage();
    }

    private void handleSmartBoxTariffRequest(VbMessage message) throws Exception {
        String chatId = message.getSender().getId();

        ButtonPayload tariffDetailsPayload = buttonPayloadsReader.getButtonPayload("smartBox detail");
        String picture = picturesReader.getPictureUrl("smartBox tariff");

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        sender.setReceiver(chatId)
                .setPicture(picture)
                .setText(smartBoxTariffPayload.getCaption() + ":\n" + tariffDetailsPayload.getUrl())
                .sendPicture();
        sender.setReceiver(chatId)
                .addButton(new ReplyButton(3, 1, expressTariffPayload.getCaption(), expressTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, standardTariffPayload.getCaption(), standardTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, internationalPayload.getCaption(), internationalPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, universalTariffPayload.getCaption(), universalTariffPayload.getCallback()))
                .addButton(new ReplyButton(6, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .sendMessage();
    }

    private void handleUniversalTariffRequest(VbMessage message) throws Exception {
        String chatId = message.getSender().getId();

        ButtonPayload tariffDetailsPayload = buttonPayloadsReader.getButtonPayload("universal detail");

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        sender.setReceiver(chatId)
                .setText(tariffDetailsPayload.getDescription() + ":\n" + tariffDetailsPayload.getUrl())
                .addButton(new ReplyButton(3, 1, expressTariffPayload.getCaption(), expressTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, standardTariffPayload.getCaption(), standardTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, internationalPayload.getCaption(), internationalPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback()))
                .addButton(new ReplyButton(6, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .sendMessage();
    }

    private void handleInternationalTariffRequest(VbMessage message) throws Exception {
        String chatId = message.getSender().getId();

        ButtonPayload tariffDetailsPayload = buttonPayloadsReader.getButtonPayload("international detail");

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        sender.setReceiver(chatId)
                .setText(tariffDetailsPayload.getDescription() + ":\n" + tariffDetailsPayload.getUrl())
                .addButton(new ReplyButton(3, 1, expressTariffPayload.getCaption(), expressTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, standardTariffPayload.getCaption(), standardTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, smartBoxTariffPayload.getCaption(), smartBoxTariffPayload.getCallback()))
                .addButton(new ReplyButton(3, 1, universalTariffPayload.getCaption(), universalTariffPayload.getCallback()))
                .addButton(new ReplyButton(6, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .sendMessage();
    }

    private void handleBarcodeRequest(VbMessage message) throws Exception {
        String chatId = message.getSender().getId();
        PackageStatus status = requestBarcodeStatus(message.getMessage().getText(), message.getSender().getId(), viberLog);
        String responseText = createStatusResponseMessage(status, message.getMessage().getText());

        if (status != null) {
            ButtonPayload detailsButtonPayload = buttonPayloadsReader.getButtonPayload("barcode details");
            Keyboard keyboard = new Keyboard();

            if (!finalStatuses.contains(status.getEvent())) {
                if (monitorDao.getStatus(message.getMessage().getText().toUpperCase(), chatId).isEmpty()) {
                    responseText += "\n" + messagesReader.getMessage("track start offer").trim();
                    ButtonPayload trackButtonPayload = buttonPayloadsReader.getButtonPayload("track start");
                    ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
                    keyboard.addButton(new ReplyButton(6, 1, trackButtonPayload.getCaption(), String.format(trackButtonPayload.getCallback(), message.getMessage().getText())))
                            .addButton(new UrlButton(3, 1, detailsButtonPayload.getCaption(), detailsButtonPayload.getUrl()))
                            .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()));
                } else {
                    responseText += "\n" + messagesReader.getMessage("track stop offer").trim();
                    ButtonPayload trackButtonPayload = buttonPayloadsReader.getButtonPayload("track stop");
                    ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
                    keyboard.addButton(new ReplyButton(6, 1, trackButtonPayload.getCaption(), String.format(trackButtonPayload.getCallback(), message.getMessage().getText())))
                            .addButton(new UrlButton(3, 1, detailsButtonPayload.getCaption(), detailsButtonPayload.getUrl()))
                            .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()));
                }
                sender.setReceiver(chatId)
                        .setText(responseText)
                        .setKeyboard(keyboard)
                        .sendMessage();
            }
        } else {
            sender.setReceiver(chatId)
                    .setText(responseText)
                    .setKeyboard(getMainKeyboard())
                    .sendMessage();
        }
    }

    private void handlePostalCodeRequest(VbMessage message) throws Exception {
        String postIndex = message.getMessage().getText();
        VPZIndex vpzIndex = new VPZIndexDao().getVPZIndex(postIndex, telegramLog);
        String chatId = message.getSender().getId();

        ButtonPayload continueSearchButtonPayload = buttonPayloadsReader.getButtonPayload("continue search");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");

        String responseText;
        if (vpzIndex == null) {
            String templateResponse = messagesReader.getMessage("office not found").trim();
            responseText = String.format(templateResponse, postIndex);

            sender.setReceiver(chatId)
                    .setText(responseText)
                    .addButton(new ReplyButton(3, 1, continueSearchButtonPayload.getCaption(), continueSearchButtonPayload.getCallback()))
                    .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
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
            sender.setReceiver(chatId)
                    .setText(responseText)
                    .sendMessage();

            try {
                sender.setReceiver(chatId)
                        .setCoordinates(vpzIndex.getPostFilialGeo().getLatitude(), vpzIndex.getPostFilialGeo().getLongitude())
                        .addButton(new ReplyButton(3, 1, continueSearchButtonPayload.getCaption(), continueSearchButtonPayload.getCallback()))
                        .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                        .sendLocation();
            } catch (NullPointerException npe) {
                viberLog.error("Coordinates not found for " + postIndex + ". Unable to send location to user.");
            }
        }
    }

    private void startTracking(VbMessage message) throws Exception {
        String[] callbackMessage = message.getMessage().getText().split(" ");
        String chatId = message.getSender().getId();

        PackageStatus currentStatus = requestBarcodeStatus(callbackMessage[1].toUpperCase(),
                chatId, viberLog);
        String status = currentStatus == null ? "не знайдене" : currentStatus.getEventName();
        monitorDao.startTracking(callbackMessage[1].toUpperCase(), chatId, status, BotType.VIBER);

        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
        ButtonPayload mainMenuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu");
        String defaultText = messagesReader.getMessage("track started").trim();
        String text = String.format(defaultText, callbackMessage[1]);

        sender.setReceiver(chatId)
                .addButton(new ReplyButton(3, 1, buttonPayload.getCaption(), String.format(buttonPayload.getCallback(), callbackMessage[1].toUpperCase())))
                .addButton(new ReplyButton(3, 1, mainMenuButtonPayload.getCaption(), mainMenuButtonPayload.getCaption()))
                .setText(text)
                .sendMessage();
    }

    private void stopTracking(VbMessage message) throws Exception {
        String[] callbackMessage = message.getMessage().getText().split(" ");
        String chatId = message.getSender().getId();

        monitorDao.stopTracking(callbackMessage[1].toUpperCase(), chatId, BotType.VIBER);

        String defaultText = messagesReader.getMessage("track stopped").trim();
        String text = String.format(defaultText, callbackMessage[1]);

        sender.setReceiver(chatId)
                .setKeyboard(getMainKeyboard())
                .setText(text)
                .sendMessage();
    }
}
