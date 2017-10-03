package ua.ukrposhta.dao;

import org.hibernate.Query;
import org.hibernate.Session;
import ua.ukrposhta.entities.PostFilial;
import ua.ukrposhta.entities.VPZIndex;
import ua.ukrposhta.utils.Loggers.BotLogger;
import ua.ukrposhta.utils.SFactory;

import java.util.Date;

public class VPZIndexDao {
    public VPZIndex getVPZIndex(String index, BotLogger log) {
        Session session = SFactory.getMerezhaInstance().openSession();
        session.beginTransaction();

        log.info("Requesting information about post office by index " + index);
        Query query = session.createQuery("from PostFilial where code = :index and dateStop > :date");
        query.setParameter("index", index + "0");
        query.setParameter("date", new Date());
        PostFilial postFilial = (PostFilial) query.uniqueResult();
        VPZIndex vpzIndex = postFilial == null ? null : postFilial.getVpzIndex();

        //session.getTransaction().commit();
        session.close();

        return vpzIndex;
    }
}
