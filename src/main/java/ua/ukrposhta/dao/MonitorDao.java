package ua.ukrposhta.dao;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import ua.ukrposhta.entities.ShkiMonitoring;
import ua.ukrposhta.utils.types.BotType;
import ua.ukrposhta.utils.SFactory;

import java.util.Date;
import java.util.List;

public class MonitorDao {
    private Logger log;

    public MonitorDao() {
        log = Logger.getLogger(MonitorDao.class);
    }

    public void startTracking(String barcode, String userId, String currentStatus, BotType botType) {
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();
        ShkiMonitoring shkiMonitoring = new ShkiMonitoring(barcode, userId, currentStatus, botType.getName());
        session.save(shkiMonitoring);
        session.getTransaction().commit();
        session.close();
    }

    public void stopTracking(String barcode, String userId, BotType botType) {
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();

        ShkiMonitoring shkiMonitoring = new ShkiMonitoring();
        shkiMonitoring.setBarcode(barcode);
        shkiMonitoring.setUserId(userId);
        shkiMonitoring.setBotType(botType.getName());
        session.delete(shkiMonitoring);

        session.getTransaction().commit();
        session.close();
    }

    public List<ShkiMonitoring> getAll() {
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();
        Query query = session.createQuery("select m from ShkiMonitoring m");
        List<ShkiMonitoring> result = query.list();
        session.getTransaction().commit();
        session.close();
        return result;
    }

    public String getStatus(String barcode, String userId) {
        String result;

        log.info("Проверка посылки " + barcode + " по базе автоматического трекинга.");
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();
        Query query = session.createQuery("from ShkiMonitoring where barcode=:barcode and userId=:userId");
        query.setParameter("barcode", barcode);
        query.setParameter("userId", userId);
        log.info("Запрос номера посылки " + barcode + " из БД shki_monitoring");
        ShkiMonitoring shkiMonitoring = (ShkiMonitoring) query.uniqueResult();
        result = shkiMonitoring == null ? "" : shkiMonitoring.getBarcode();
        if (result.isEmpty()) {
            log.warn("Запрос номера посылки " + barcode + " из БД shki_monitoring вернул пустой ответ");
        } else {
            log.info("Запрос номера посылки " + barcode + " из БД shki_monitoring вернул не пустой ответ");
        }
        session.getTransaction().commit();
        session.close();
        return result;
    }

    public void updateStatus(String barcode, String userId, String newStatus) {
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();

        Query query = session.createQuery("update ShkiMonitoring set status=:newStatus, dateUpdate = :dateUpdate where barcode=:barcode and userId=:userId");
        query.setParameter("newStatus", newStatus);
        query.setParameter("dateUpdate", new Date());
        query.setParameter("barcode", barcode);
        query.setParameter("userId", userId);
        query.executeUpdate();

        session.getTransaction().commit();
        session.close();
    }

    public void removeBarcode(ShkiMonitoring barcode) {
        Session session = SFactory.getMonitorInstance().openSession();
        session.beginTransaction();

        session.delete(barcode);

        session.getTransaction().commit();
        session.close();
    }
}
