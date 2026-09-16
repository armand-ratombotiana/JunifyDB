package org.junify.db.jpa;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.metamodel.Metamodel;
import org.junify.db.JunifyDB;
import org.junify.db.adapter.jnosql.EclipseDocumentTemplate;
import org.junify.db.adapter.jnosql.EntityMapper;
import org.junify.db.nosql.document.Document;
import org.junify.db.nosql.document.DocumentCollection;

import java.util.*;

public class JunifyEntityManager implements EntityManager {

    private final JunifyDB db;
    private final JunifyEntityManagerFactory factory;
    private final JunifyEntityTransaction transaction;
    private final Map<Object, Object> persistenceContext = new IdentityHashMap<>();
    private FlushModeType flushMode = FlushModeType.AUTO;
    private volatile boolean open = true;

    public JunifyEntityManager(JunifyDB db, JunifyEntityManagerFactory factory) {
        this.db = db;
        this.factory = factory;
        this.transaction = new JunifyEntityTransaction(db);
    }

    private void checkOpen() {
        if (!open) {
            throw new IllegalStateException("EntityManager is closed");
        }
    }

    private DocumentCollection getCollection(String name) {
        if (transaction.isActive() && transaction.getActiveTx() != null) {
            return transaction.getActiveTx().documentCollection(name);
        }
        return db.documentCollection(name);
    }

    @Override
    public void persist(Object entity) {
        checkOpen();
        Objects.requireNonNull(entity, "Entity cannot be null");
        String colName = EntityMapper.getCollectionName(entity.getClass());
        DocumentCollection col = getCollection(colName);

        Document doc = EntityMapper.toDocument(entity, false);
        col.insert(doc);
        persistenceContext.put(entity, EntityMapper.getIdValue(entity));
    }

    @Override
    public <T> T merge(T entity) {
        checkOpen();
        Objects.requireNonNull(entity, "Entity cannot be null");
        String colName = EntityMapper.getCollectionName(entity.getClass());
        DocumentCollection col = getCollection(colName);

        Document doc = EntityMapper.toDocument(entity, true);
        col.update(doc);
        persistenceContext.put(entity, EntityMapper.getIdValue(entity));
        return entity;
    }

    @Override
    public void remove(Object entity) {
        checkOpen();
        Objects.requireNonNull(entity, "Entity cannot be null");
        String colName = EntityMapper.getCollectionName(entity.getClass());
        DocumentCollection col = getCollection(colName);

        Object id = EntityMapper.getIdValue(entity);
        if (id != null) {
            col.deleteById(id.toString());
            persistenceContext.remove(entity);
        }
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return find(entityClass, primaryKey, Collections.emptyMap());
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        checkOpen();
        Objects.requireNonNull(entityClass, "Entity class cannot be null");
        if (primaryKey == null) return null;

        String colName = EntityMapper.getCollectionName(entityClass);
        DocumentCollection col = getCollection(colName);
        Document doc = col.findById(primaryKey.toString());
        if (doc == null) return null;

        T entity = EntityMapper.fromDocument(doc, entityClass);
        persistenceContext.put(entity, primaryKey);
        return entity;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return find(entityClass, primaryKey);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return find(entityClass, primaryKey);
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return find(entityClass, primaryKey);
    }

    @Override
    public void flush() {
        checkOpen();
        // JunifyDB collections write immediately or through transaction buffer
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        checkOpen();
        this.flushMode = flushMode;
    }

    @Override
    public FlushModeType getFlushMode() {
        checkOpen();
        return flushMode;
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        checkOpen();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        checkOpen();
    }

    @Override
    public void refresh(Object entity) {
        checkOpen();
        Object id = EntityMapper.getIdValue(entity);
        if (id != null) {
            String colName = EntityMapper.getCollectionName(entity.getClass());
            Document doc = db.documentCollection(colName).findById(id.toString());
            if (doc != null) {
                Object refreshed = EntityMapper.fromDocument(doc, entity.getClass());
                // Refresh entity fields
                for (var f : entity.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    try {
                        f.set(entity, f.get(refreshed));
                    } catch (Exception ignored) {}
                }
            }
        }
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        refresh(entity);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        refresh(entity);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        refresh(entity);
    }

    @Override
    public void clear() {
        checkOpen();
        persistenceContext.clear();
    }

    @Override
    public void detach(Object entity) {
        checkOpen();
        persistenceContext.remove(entity);
    }

    @Override
    public boolean contains(Object entity) {
        checkOpen();
        if (entity == null) return false;
        Object id = EntityMapper.getIdValue(entity);
        if (id == null) return false;
        String colName = EntityMapper.getCollectionName(entity.getClass());
        return getCollection(colName).findById(id.toString()) != null;
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return LockModeType.NONE;
    }

    @Override
    public void setProperty(String propertyName, Object value) {}

    @Override
    public Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

    @Override
    public Query createQuery(String qlString) {
        return createQuery(qlString, Object.class);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        throw new UnsupportedOperationException("CriteriaQuery is not yet supported. Use JPQL / SQL string queries.");
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        throw new UnsupportedOperationException("CriteriaUpdate is not yet supported.");
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        throw new UnsupportedOperationException("CriteriaDelete is not yet supported.");
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        checkOpen();
        return new JunifyTypedQuery<>(db, qlString, resultClass);
    }

    @Override
    public Query createNamedQuery(String name) {
        throw new UnsupportedOperationException("Named queries not supported yet");
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        throw new UnsupportedOperationException("Named queries not supported yet");
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        checkOpen();
        return new JunifyTypedQuery<>(db, sqlString, Object[].class);
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultClass) {
        checkOpen();
        return new JunifyTypedQuery<>(db, sqlString, resultClass);
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return createNativeQuery(sqlString);
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        throw new UnsupportedOperationException("Stored procedures not supported");
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        throw new UnsupportedOperationException("Stored procedures not supported");
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        throw new UnsupportedOperationException("Stored procedures not supported");
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        throw new UnsupportedOperationException("Stored procedures not supported");
    }

    @Override
    public void joinTransaction() {}

    @Override
    public boolean isJoinedToTransaction() {
        return transaction.isActive();
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isInstance(this)) return cls.cast(this);
        if (cls.isInstance(db)) return cls.cast(db);
        throw new PersistenceException("Cannot unwrap to " + cls.getName());
    }

    @Override
    public Object getDelegate() {
        return db;
    }

    @Override
    public void close() {
        this.open = false;
        persistenceContext.clear();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public EntityTransaction getTransaction() {
        return transaction;
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return factory;
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
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        throw new UnsupportedOperationException("EntityGraph not supported");
    }

    @Override
    public EntityGraph<?> createEntityGraph(String graphName) {
        throw new UnsupportedOperationException("EntityGraph not supported");
    }

    @Override
    public EntityGraph<?> getEntityGraph(String graphName) {
        throw new UnsupportedOperationException("EntityGraph not supported");
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return Collections.emptyList();
    }
}
