package dev.denux.clanmanager.core.sql;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class SqlSession {

    public static SessionFactory buildSessionFactory() {
        Configuration conf = new Configuration();
        Properties props = new Properties();
        props.put("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        conf.addProperties(props);
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(conf.getProperties()).build();
        return conf.buildSessionFactory(serviceRegistry);
    }
}
