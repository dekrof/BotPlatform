package ua.ukrposhta.utils.Loggers;

import org.apache.log4j.Logger;

public class TelegramLogger extends BotLogger{
    private static TelegramLogger instance;
    private Logger logger = Logger.getLogger("telegramLogger");

    private TelegramLogger() {

    }

    public static TelegramLogger getInstance() {
        if (instance == null) {
            instance = new TelegramLogger();
        }

        return instance;
    }

    public void info(Object message) {
        logger.info(message);
        consoleLogger.info(message);
    }

    public void warn(Object message) {
        logger.warn(message);
        consoleLogger.warn(message);
    }

    public void error(Object message) {
        logger.error(message);
        consoleLogger.error(message);
    }
}
