package ua.ukrposhta.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import ua.ukrposhta.entities.TelegramUser;
import ua.ukrposhta.models.telegram.TgMessage;
import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.SFactory;

public class TelegramUserDao {
    private BotLogger log = BotLogger.getLogger(LoggerType.TELEGRAM);

    public void addNewUser(TgMessage message) {
        int userId = message.getSender().getId();

        log.info("Проверка пользователя " + userId + " по базе.");
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();
        Query query = session.createQuery("from TelegramUser where userId=:userId");
        query.setParameter("userId", userId);
        TelegramUser telegramUser = (TelegramUser) query.uniqueResult();
        session.getTransaction().commit();
        if (telegramUser != null) {
            log.info("Пользователь " + userId + " уже есть в базе.");
            session.close();
            return;
        }
        telegramUser = new TelegramUser(
                message.getSender().getId(),
                message.getSender().getFirstName(),
                message.getSender().getLastName(),
                message.getChat().getId()
        );
        session.beginTransaction();
        session.save(telegramUser);
        session.getTransaction().commit();
        log.info("Пользователь: "
                + message.getSender().getFirstName()
                + " "
                + message.getSender().getLastName()
                + ", id:"
                + message.getSender().getId()
                + ", добавлен в базу.");
        session.close();
    }
}
