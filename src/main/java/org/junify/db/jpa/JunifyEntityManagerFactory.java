package org.junify.db.jpa;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;
import org.junify.db.JunifyDB;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JunifyEntityManagerFactory implements EntityManagerFactory {

    private final JunifyDB db;
    private final Map<String, Object> properties = new ConcurrentHashMap<>();
    private volatile boolean open = true;

    public JunifyEntityManagerFactory(JunifyDB db) {
        this.db = db;
    }

    @Override
    public EntityManager createEntityManager() {
        if (!open) throw new IllegalStateException("EntityManagerFactory is closed");
        return new JunifyEntityManager(db, this);
    }

    @Override
    public EntityManager createEntityManager(Map map) {
        return createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType) {
        return createEntityManager();
    }

    @Override
    public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) {
        return createEntityManager();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        throw new UnsupportedOperationException("CriteriaBuilder not supported; use createQuery(sql, Class)");
    }

    @Override
    public Metamodel getMetamodel() {
        throw new UnsupportedOperationException("Metamodel not supported");
    }

    @Override
    public boolean isOpen() {
        return open && !db.isClosed();
    }

    @Override
    public void close() {
        this.open = false;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public Cache getCache() {
        throw new UnsupportedOperationException("Cache not supported");
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        throw new UnsupportedOperationException("PersistenceUnitUtil not supported");
    }

    @Override
    public void addNamedQuery(String name, Query query) {}

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isInstance(this)) return cls.cast(this);
        if (cls.isInstance(db)) return cls.cast(db);
        throw new PersistenceException("Cannot unwrap to " + cls.getName());
    }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) {}

    public JunifyDB getDb() {
        return db;
    }
}
