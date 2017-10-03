package ua.ukrposhta.utils.Loggers;

import org.apache.log4j.Logger;

public class MonitorLogger extends BotLogger {
    private static MonitorLogger instance;
    private Logger logger = Logger.getLogger("monitorLogger");

    private MonitorLogger() {

    }

    public static MonitorLogger getInstance() {
        if (instance == null) {
            instance = new MonitorLogger();
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
