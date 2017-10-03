package ua.ukrposhta.utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class SFactory {
    private static SessionFactory monitorInstance;
    private static SessionFactory merezhaInstance;

    private SFactory() {

    }

    public static SessionFactory getMonitorInstance() {
        if (monitorInstance == null) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("dbConfig/chatbot_db.cfg.xml")
                    .build();
            monitorInstance = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        return monitorInstance;
    }

    public static SessionFactory getMerezhaInstance() {
        if (merezhaInstance == null) {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                    .configure("dbConfig/merezha.cfg.xml")
                    .build();
            merezhaInstance = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        return merezhaInstance;
    }
}
