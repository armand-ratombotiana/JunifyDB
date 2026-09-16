package org.junify.db.jpa;

import jakarta.persistence.EntityManagerFactory;
import org.junify.db.JunifyDB;
import org.junify.db.config.JunifyDBConfig;

import java.util.Map;

/**
 * Bootstrap entry point for creating JPA EntityManagerFactory backed by JunifyDB.
 */
public final class JunifyPersistence {

    private JunifyPersistence() {}

    public static EntityManagerFactory createEntityManagerFactory(JunifyDB db) {
        return new JunifyEntityManagerFactory(db);
    }

    public static jakarta.persistence.EntityManager createEntityManager(JunifyDB db) {
        return createEntityManagerFactory(db).createEntityManager();
    }

    public static EntityManagerFactory createEntityManagerFactory(JunifyDBConfig config) {
        JunifyDB db = JunifyDB.create(config);
        return new JunifyEntityManagerFactory(db);
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        return createEntityManagerFactory(JunifyDB.embed()
                .storageEngine(JunifyDBConfig.StorageEngineType.IN_MEMORY)
                .buildConfig());
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, Map<?, ?> properties) {
        return createEntityManagerFactory(persistenceUnitName);
    }
}
