package ua.ukrposhta.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import ua.ukrposhta.entities.FacebookUser;
import ua.ukrposhta.models.facebook.FacebookUserProfile;
import ua.ukrposhta.utils.types.LoggerType;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.SFactory;

import java.util.List;

public class FacebookUserDao {
    private BotLogger log = BotLogger.getLogger(LoggerType.FACEBOOK);

    public List<FacebookUser> getAllUsers() {
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();
        Query query = session.createQuery("from FacebookUser");
        List<FacebookUser> users = query.list();
        session.getTransaction().commit();
        session.close();
        return users;
    }

    public void addNewUser(String userId, FacebookUserProfile userProfile) {
        log.info("Checking if user " + userId + " exists in database.");

        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();
        Query query = session.createQuery("from FacebookUser where userId=:userId");
        query.setParameter("userId", userId);
        FacebookUser facebookUser = (FacebookUser) query.uniqueResult();
        session.getTransaction().commit();
        if (facebookUser != null) {
            log.info("User " + userId + " already exists in database.");
            session.close();
            return;
        }

        facebookUser = new FacebookUser(
                userId,
                userProfile.getFirstName(),
                userProfile.getLastName());

        session.beginTransaction();
        session.save(facebookUser);
        session.getTransaction().commit();
        session.close();
        log.info("User "
                + userProfile.getFirstName()
                + " "
                + userProfile.getLastName()
                +", id "
                + userId
                + ", added to the database.");
    }
}
