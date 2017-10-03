package ua.ukrposhta.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import ua.ukrposhta.entities.ViberUser;
import ua.ukrposhta.models.viber.Sender;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.SFactory;
import ua.ukrposhta.utils.types.LoggerType;

public class ViberUserDao {
    private BotLogger log = BotLogger.getLogger(LoggerType.VIBER);

    public void addNewUser(Sender sender) {
        if (sender == null) {
            log.error("Unable to add new user to the database because sender is not specified");
            return;
        }
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();

        log.info("Checking if user '" + sender.getId() + "', " + sender.getName() + " is already in database.");
        Query query = session.createQuery("from ViberUser where id=:id");
        query.setParameter("id", sender.getId());
        ViberUser viberUser = (ViberUser) query.uniqueResult();
        session.getTransaction().commit();

        if (viberUser != null) {
            log.info("User '" + sender.getId() + "', " + sender.getName() + " is already in database.");
            session.close();
            return;
        }

        viberUser = new ViberUser()
                .setId(sender.getId())
                .setName(sender.getName())
                .setAvatarUrl(sender.getAvatarUrl())
                .setLanguage(sender.getLanguage())
                .setCountry(sender.getCountry())
                .setApiVersion(sender.getApiVersion());

        session.beginTransaction();
        session.save(viberUser);
        session.getTransaction().commit();
        log.info("User '" + sender.getId() + "', " + sender.getName() + " added to the database.");

        session.close();
    }
}
