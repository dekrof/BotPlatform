package ua.ukrposhta.bot;

import ua.ukrposhta.dao.MonitorDao;
import ua.ukrposhta.dao.TelegramUserDao;
import ua.ukrposhta.dao.UnknownMessageDao;
import ua.ukrposhta.dao.VPZIndexDao;
import ua.ukrposhta.entities.VPZIndex;
import ua.ukrposhta.entities.WorkSchedule;
import ua.ukrposhta.models.PackageStatus;
import ua.ukrposhta.models.telegram.Callback;
import ua.ukrposhta.models.telegram.TgMessage;
import ua.ukrposhta.models.telegram.keyboard.*;
import ua.ukrposhta.models.texts.ButtonPayload;
import ua.ukrposhta.utils.senders.TelegramSender;
import ua.ukrposhta.utils.types.BotType;
import ua.ukrposhta.utils.readers.ButtonPayloadType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TelegramBot extends Bot {
    private TelegramUserDao telegramUserDao = new TelegramUserDao();
    private MonitorDao monitorDao = new MonitorDao();

    private static TelegramBot instance;
    private TelegramSender sender;
    private Keyboard mainMenuKeyboard;


    private TelegramBot() {
        sender = TelegramSender.getInstance();

        try {
            ButtonPayload trackingButtonPayload = buttonPayloadsReader.getButtonPayload("menu tracking");
            ButtonPayload officesButtonPayload = buttonPayloadsReader.getButtonPayload("menu offices");
            ButtonPayload tariffsButtonPayload = buttonPayloadsReader.getButtonPayload("menu tariffs");
            ButtonPayload helpButtonPayload = buttonPayloadsReader.getButtonPayload("menu help");

            mainMenuKeyboard = new ReplyKeyboard()
                    .addRow()
                    .addRow()
                    .addButton(0, new ReplyKeyboardButton(trackingButtonPayload.getCaption()))
                    .addButton(0, new ReplyKeyboardButton(officesButtonPayload.getCaption()))
                    .addButton(1, new ReplyKeyboardButton(tariffsButtonPayload.getCaption()))
                    .addButton(1, new ReplyKeyboardButton(helpButtonPayload.getCaption()))
                    .setResizeKeyboard(true)
                    .setOneTimeKeyboard(true);
        } catch (Exception e) {
            telegramLog.error(e);
        }
    }

    public static TelegramBot getInstance() {
        if (instance == null) {
            instance = new TelegramBot();
        }
        return instance;
    }

    public void handleRedirect(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        String response = messagesReader.getMessage("redirect").trim();
        sender.setChatId(chatId)
                .setText(response)
                .sendRedirect();
    }

    public void handleMessage(TgMessage message) {
        String chatId = Integer.toString(message.getChat().getId());

        if (!message.getText().isEmpty()) {
            telegramUserDao.addNewUser(message);
            try {
                switch (message.getText().toLowerCase()) {
                    case "/start": {
                        String text = messagesReader.getMessage("start").trim();
                        sender.setChatId(chatId)
                                .setText(text)
                                .setReplyMarkup(mainMenuKeyboard)
                                .sendMessage();
                        break;
                    }
                    case "трекінг": {
                        handleTrackingHelp(message);
                        break;
                    }
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
                    case "стандарт": {
                        handleStandardTariffRequest(message);
                        break;
                    }
                    case "експрес": {
                        handleExpressTariffRequest(message);
                        break;
                    }
                    case "smartbox": {
                        handleSmartBoxTariffRequest(message);
                        break;
                    }
                    case "універсальні":
                    case "універсальні послуги": {
                        handleUniversalServicesRequest(message);
                        break;
                    }
                    case "міжнародні": {
                        handleInternationalTariffRequest(message);
                        break;
                    }
                    case "головне меню": {
                        handleMainMenuRequest(message);
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
                        sender.setChatId(chatId)
                                .setText(text)
                                .setReplyMarkup(mainMenuKeyboard)
                                .sendMessage();
                        break;
                    }
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
                        ButtonPayload helpPayload = buttonPayloadsReader.getButtonPayload("menu help");
                        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");
                        Keyboard replyKeyboard = new ReplyKeyboard()
                                .setResizeKeyboard(true)
                                .setOneTimeKeyboard(true)
                                .addRow()
                                .addButton(0, new ReplyKeyboardButton(helpPayload.getCaption()))
                                .addButton(0, new ReplyKeyboardButton(mainMenuPayload.getCaption()));
                        sender.setChatId(chatId)
                                .setText(text)
                                .setReplyMarkup(replyKeyboard)
                                .sendMessage();

                        new UnknownMessageDao().addMessage(chatId, message.getText(), text, BotType.TELEGRAM);
                    }
                }
            } catch (Exception e) {
                telegramLog.error(e);
            }
        }
    }

    public void handleCallback(Callback callback) {
        String chatId = Integer.toString(callback.getSender().getId());
        String messageId = Integer.toString(callback.getMessage().getMessageId());

        try {
            switch (callback.getText().toLowerCase()) {
                case "головне меню": {
                    handleMainMenuRequest(callback.getMessage());
                    break;
                }
                case "відділення": {
                    handlePostalCodeHelp(callback.getMessage());
                    return;
                }
                default: {
                    Pattern startTrackingPattern = Pattern.compile("відстежувати.+");
                    Pattern stopTrackingPattern = Pattern.compile("припинити.+");
                    Matcher matcher = startTrackingPattern.matcher(callback.getText());

                    if (matcher.matches()) {
                        String[] callbackMessage = callback.getText().split(" ");

                        PackageStatus currentStatus = requestBarcodeStatus(callbackMessage[1].toUpperCase(),
                                Integer.toString(callback.getSender().getId()), telegramLog);
                        String status = currentStatus == null ? "не знайдене" : currentStatus.getEventName();
                        monitorDao.startTracking(callbackMessage[1].toUpperCase(), chatId, status, BotType.TELEGRAM);

                        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
                        String defaultText = messagesReader.getMessage("track started").trim();
                        String text = String.format(defaultText, callbackMessage[1]);

                        Keyboard keyboard = new InlineKeyboard()
                                .addRow()
                                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                                        .setCallback(String.format(buttonPayload.getCallback(), callbackMessage[1])));
                        sender.setChatId(chatId)
                                .setMessageId(messageId)
                                .setText(text)
                                .setReplyMarkup(keyboard)
                                .editMessage();
                        return;
                    }
                    matcher = stopTrackingPattern.matcher(callback.getText());
                    if (matcher.matches()) {
                        String[] callbackMessage = callback.getText().split(" ");

                        monitorDao.stopTracking(callbackMessage[1].toUpperCase(), chatId, BotType.TELEGRAM);
                        String defaultText = messagesReader.getMessage("track stopped").trim();
                        String text = String.format(defaultText, callbackMessage[1]);
                        sender.setChatId(chatId)
                                .setMessageId(messageId)
                                .setText(text)
                                .editMessage();
                    }
                }
            }
        } catch (Exception e) {
            telegramLog.error(e);
        }
    }

    public void sendBarcodeUpdate(String userId, PackageStatus newStatus, PackageStatus prevStatus) throws Exception {
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");
        String defaultText = messagesReader.getMessage("track update").trim();
        String text = String.format(defaultText, newStatus.getBarcode(),
                prevStatus.getEventName(), prevStatus.getName(), prevStatus.getIndex(),
                newStatus.getEventName(), newStatus.getName(), newStatus.getIndex());
        InlineKeyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                        .setCallback(String.format(buttonPayload.getCallback(), newStatus.getBarcode())));
        sender.setChatId(userId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();

        text = messagesReader.getMessage("alternate select").trim();
        sender.setChatId(userId)
                .setText(text)
                .setReplyMarkup(mainMenuKeyboard)
                .sendMessage();
    }

// *******************************************************************************************************************
//
// Inner handle methods which are called from handleMessage or handleCallback
//
// *******************************************************************************************************************

    private void handleMainMenuRequest(TgMessage message) {
        String chatId = Integer.toString(message.getChat().getId());
        sender.setChatId(chatId)
                .setText("Головне меню")
                .setReplyMarkup(mainMenuKeyboard)
                .sendMessage();
    }

    private void handlePostalCodeHelp(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        String text = messagesReader.getMessage("postal code help").trim();
        sender.setChatId(chatId)
                .setText(text)
                .sendMessage();
    }

    private void handleTrackingHelp(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        String text = messagesReader.getMessage("tracking help").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(null)
                .sendMessage();
    }

    private void handleTariffsRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalTariffPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");

        Keyboard menuKeyboard = new ReplyKeyboard()
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
                .addRow()
                .addRow()
                .addRow()
                .addButton(0, new ReplyKeyboardButton(expressTariffPayload.getCaption()))
                .addButton(0, new ReplyKeyboardButton(standardTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(internationalTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(smartBoxTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(universalTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(mainMenuPayload.getCaption()));

        String text = messagesReader.getMessage("tariffs request").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(menuKeyboard)
                .sendMessage();
    }

    private void handleStandardTariffRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("standard detail");
        String picture = picturesReader.getPictureUrl("standard tariff");
        Keyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                        .setUrl(buttonPayload.getUrl()));
        sender.setChatId(chatId)
                .setPhoto(picture)
                .setReplyMarkup(keyboard)
                .sendPhoto();

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload internationalTariffPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");
        keyboard = new ReplyKeyboard()
                .addRow()
                .addRow()
                .addRow()
                .addButton(0, new ReplyKeyboardButton(expressTariffPayload.getCaption()))
                .addButton(0, new ReplyKeyboardButton(internationalTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(smartBoxTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(universalTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(mainMenuPayload.getCaption()))
                .setResizeKeyboard(true);
        String text = messagesReader.getMessage("other tariffs").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();
    }

    private void handleExpressTariffRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("express detail");
        String picture = picturesReader.getPictureUrl("express tariff");
        Keyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                        .setUrl(buttonPayload.getUrl()));
        sender.setChatId(chatId)
                .setPhoto(picture)
                .setReplyMarkup(keyboard)
                .sendPhoto();

        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalTariffPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");
        keyboard = new ReplyKeyboard()
                .addRow()
                .addRow()
                .addRow()
                .addButton(0, new ReplyKeyboardButton(standardTariffPayload.getCaption()))
                .addButton(0, new ReplyKeyboardButton(internationalTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(smartBoxTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(universalTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(mainMenuPayload.getCaption()))
                .setResizeKeyboard(true);
        String text = messagesReader.getMessage("other tariffs").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();
    }

    private void handleSmartBoxTariffRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("smartBox detail");
        String picture = picturesReader.getPictureUrl("smartBox tariff");
        Keyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                        .setUrl(buttonPayload.getUrl()));
        sender.setChatId(chatId)
                .setPhoto(picture)
                .setReplyMarkup(keyboard)
                .sendPhoto();

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalTariffPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");
        keyboard = new ReplyKeyboard()
                .addRow()
                .addRow()
                .addRow()
                .addButton(0, new ReplyKeyboardButton(expressTariffPayload.getCaption()))
                .addButton(0, new ReplyKeyboardButton(standardTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(internationalTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(universalTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(mainMenuPayload.getCaption()))
                .setResizeKeyboard(true);
        String text = messagesReader.getMessage("other tariffs").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();
    }

    private void handleUniversalServicesRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("universal detail");
        String text = messagesReader.getMessage("universal tariff").trim();
        Keyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                        .setUrl(buttonPayload.getUrl()));
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload internationalTariffPayload = buttonPayloadsReader.getButtonPayload("international tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");
        keyboard = new ReplyKeyboard()
                .addRow()
                .addRow()
                .addRow()
                .addButton(0, new ReplyKeyboardButton(expressTariffPayload.getCaption()))
                .addButton(0, new ReplyKeyboardButton(standardTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(internationalTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(smartBoxTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(mainMenuPayload.getCaption()))
                .setResizeKeyboard(true);
        text = messagesReader.getMessage("other tariffs").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();
    }

    public void handleInternationalTariffRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("international detail");
        String text = messagesReader.getMessage("international tariff").trim();
        Keyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                        .setUrl(buttonPayload.getUrl()));
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();

        ButtonPayload expressTariffPayload = buttonPayloadsReader.getButtonPayload("express tariff");
        ButtonPayload standardTariffPayload = buttonPayloadsReader.getButtonPayload("standard tariff");
        ButtonPayload smartBoxTariffPayload = buttonPayloadsReader.getButtonPayload("smartBox tariff");
        ButtonPayload universalTariffPayload = buttonPayloadsReader.getButtonPayload("universal tariff");
        ButtonPayload mainMenuPayload = buttonPayloadsReader.getButtonPayload("main menu");
        keyboard = new ReplyKeyboard()
                .addRow()
                .addRow()
                .addRow()
                .addButton(0, new ReplyKeyboardButton(expressTariffPayload.getCaption()))
                .addButton(0, new ReplyKeyboardButton(standardTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(smartBoxTariffPayload.getCaption()))
                .addButton(1, new ReplyKeyboardButton(universalTariffPayload.getCaption()))
                .addButton(2, new ReplyKeyboardButton(mainMenuPayload.getCaption()))
                .setResizeKeyboard(true);
        text = messagesReader.getMessage("other tariffs").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();
    }

    private void handleHelpRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());

        String text = messagesReader.getMessage("help").trim();
        ButtonPayload openSiteButtonPayload = buttonPayloadsReader.getButtonPayload("open site");
        Keyboard keyboard = new InlineKeyboard()
                .addRow()
                .addButton(0, new InlineKeyboardButton(openSiteButtonPayload.getCaption())
                        .setUrl(openSiteButtonPayload.getUrl()));
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .sendMessage();

        text = messagesReader.getMessage("alternate select").trim();
        sender.setChatId(chatId)
                .setText(text)
                .setReplyMarkup(mainMenuKeyboard)
                .sendMessage();
    }

    private void handleBarcodeRequest(TgMessage message) throws Exception {
        String chatId = Integer.toString(message.getChat().getId());
        PackageStatus status = requestBarcodeStatus(message.getText(),
                Integer.toString(message.getSender().getId()), telegramLog);
        String responseText = createStatusResponseMessage(status, message.getText());

        if (status != null) {
            ButtonPayload buttonPayload = buttonPayloadsReader.getButtonPayload("barcode details");
            Keyboard replyKeyboard = new InlineKeyboard()
                    .addRow()
                    .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                            .setUrl(buttonPayload.getUrl()));
            sender.setChatId(chatId)
                    .setText(responseText)
                    .setReplyMarkup(replyKeyboard)
                    .sendMessage();

            if (!finalStatuses.contains(status.getEvent())) {
                if (monitorDao.getStatus(message.getText(), chatId).isEmpty()) {
                    responseText = messagesReader.getMessage("track start offer").trim();
                    buttonPayload = buttonPayloadsReader.getButtonPayload("track start");

                    replyKeyboard = new InlineKeyboard()
                            .addRow()
                            .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                                    .setCallback(String.format(buttonPayload.getCallback(), message.getText())));
                    sender.setChatId(chatId)
                            .setText(responseText)
                            .setReplyMarkup(replyKeyboard)
                            .sendMessage();
                } else {
                    responseText = messagesReader.getMessage("track stop offer").trim();
                    buttonPayload = buttonPayloadsReader.getButtonPayload("track stop");

                    replyKeyboard = new InlineKeyboard()
                            .addRow()
                            .addButton(0, new InlineKeyboardButton(buttonPayload.getCaption())
                                    .setCallback(String.format(buttonPayload.getCallback(), message.getText())));
                    sender.setChatId(chatId)
                            .setText(responseText)
                            .setReplyMarkup(replyKeyboard)
                            .sendMessage();
                }
            }
            String text = messagesReader.getMessage("alternate select").trim();
            sender.setChatId(chatId)
                    .setText(text)
                    .setReplyMarkup(mainMenuKeyboard)
                    .sendMessage();
        } else {
            sender.setChatId(chatId)
                    .setText(responseText)
                    .setReplyMarkup(mainMenuKeyboard)
                    .sendMessage();
        }
    }

    private void handlePostalCodeRequest(TgMessage message) throws Exception {
        String postIndex = message.getText();
        VPZIndex vpzIndex = new VPZIndexDao().getVPZIndex(postIndex, telegramLog);
        String chatId = Integer.toString(message.getChat().getId());

        String responseText;
        if (vpzIndex == null) {
            String templateResponse = messagesReader.getMessage("office not found").trim();
            responseText = String.format(templateResponse, postIndex);
            ButtonPayload continueSearchButtonPayload = buttonPayloadsReader.getButtonPayload("continue search");
            ButtonPayload menuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu", ButtonPayloadType.CALLBACK);

            Keyboard keyboard = new InlineKeyboard()
                    .addRow()
                    .addButton(0, new InlineKeyboardButton(continueSearchButtonPayload.getCaption())
                            .setCallback(continueSearchButtonPayload.getCallback()))
                    .addButton(0, new InlineKeyboardButton(menuButtonPayload.getCaption())
                            .setCallback(menuButtonPayload.getCallback()));
            sender.setChatId(chatId)
                    .setReplyMarkup(keyboard)
                    .setText(responseText)
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

            sender.setChatId(chatId)
                    .setText(responseText)
                    .sendMessage();
            ButtonPayload continueSearchButtonPayload = buttonPayloadsReader.getButtonPayload("continue search");
            ButtonPayload menuButtonPayload = buttonPayloadsReader.getButtonPayload("main menu", ButtonPayloadType.CALLBACK);
            Keyboard keyboard = new InlineKeyboard()
                    .addRow()
                    .addButton(0, new InlineKeyboardButton(continueSearchButtonPayload.getCaption())
                            .setCallback(continueSearchButtonPayload.getCallback()))
                    .addButton(0, new InlineKeyboardButton(menuButtonPayload.getCaption())
                            .setCallback(menuButtonPayload.getCallback()));
            try {
                this.sender.setChatId(chatId)
                        .setLatitude(vpzIndex.getPostFilialGeo().getLatitude())
                        .setLongitude(vpzIndex.getPostFilialGeo().getLongitude())
                        .setReplyMarkup(keyboard)
                        .sendLocation();
            } catch (NullPointerException npe) {
                telegramLog.error("Coordinates not found for " + postIndex + ". Unable to send location to user.");
            }
        }
    }
}
