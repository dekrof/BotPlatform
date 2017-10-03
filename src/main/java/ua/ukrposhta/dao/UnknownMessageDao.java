package ua.ukrposhta.dao;

import org.hibernate.Session;
import ua.ukrposhta.entities.UnknownMessage;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.SFactory;
import ua.ukrposhta.utils.types.BotType;
import ua.ukrposhta.utils.types.LoggerType;

public class UnknownMessageDao {

    public void addMessage(String userId, String message, String response, BotType botType) {
        BotLogger log;
        switch (botType) {
            case TELEGRAM: {
                log = BotLogger.getLogger(LoggerType.TELEGRAM);
                break;
            }
            case FACEBOOK: {
                log = BotLogger.getLogger(LoggerType.FACEBOOK);
                break;
            }
            case VIBER: {
                log = BotLogger.getLogger(LoggerType.VIBER);
                break;
            }
            default:
                // make compiler happy =)
                log = null;
        }

        UnknownMessage unknownMessage = new UnknownMessage()
                .setUserId(userId)
                .setRequest(message)
                .setResponse(response)
                .setBotType(botType.getName());

        Session session = SFactory.getMonitorInstance().openSession();
        try {
            session.beginTransaction();
            session.save(unknownMessage);
            log.info("Adding unknown message to database: " + message);
            session.getTransaction().commit();
            log.info("Successfully added unknown message to database.");
        } catch (Exception e) {
            log.error(e);
            session.getTransaction().rollback();
        } finally {
            session.close();
        }
    }
}
