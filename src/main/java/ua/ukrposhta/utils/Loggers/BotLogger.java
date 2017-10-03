package ua.ukrposhta.utils.Loggers;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import ua.ukrposhta.utils.types.LoggerType;

import java.io.IOException;
import java.util.Properties;

public abstract class BotLogger {
    final Logger consoleLogger = Logger.getLogger("consoleLogger");

    public static BotLogger getLogger(LoggerType type) {
        Properties logProperties = new Properties();
        try {
            logProperties.load(BotLogger.class.getClassLoader().getResourceAsStream("properties/log4j.properties"));
            PropertyConfigurator.configure(logProperties);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BotLogger logger = null;
        switch (type) {
            case TELEGRAM:
                logger = TelegramLogger.getInstance();
                break;
            case VIBER:
                logger = ViberLogger.getInstance();
                break;
            case FACEBOOK:
                logger = FacebookLogger.getInstance();
                break;
            case MONITOR:
                logger = MonitorLogger.getInstance();
                break;
            default:
        }
        return logger;
    }

    public abstract void info(Object message);

    public abstract void warn(Object message);

    public abstract void error(Object message);
}
