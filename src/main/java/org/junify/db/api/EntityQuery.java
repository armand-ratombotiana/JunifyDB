package org.junify.db.api;

import org.junify.db.JunifyDB;
import org.junify.db.adapter.jnosql.EntityMapper;
import org.junify.db.sql.SqlResultSet;

import java.util.*;
import java.util.stream.Stream;

/**
 * Fluent, type-safe entity query builder designed for Java developer productivity.
 *
 * <p>Example usage:
 * <pre>{@code
 * List<Product> cheap = db.from(Product.class)
 *                         .where("price < ?", 25.0)
 *                         .orderBy("name")
 *                         .limit(10)
 *                         .list();
 *
 * Optional<Product> item = db.from(Product.class)
 *                            .where("id = ?", "p-100")
 *                            .first();
 * }</pre>
 *
 * @param <T> The target Java entity or record class.
 */
public class EntityQuery<T> {

    private final JunifyDB db;
    private final Class<T> entityClass;
    private final String collectionName;
    private String whereClause;
    private final List<Object> parameters = new ArrayList<>();
    private String orderByField;
    private boolean ascending = true;
    private int limit = -1;
    private int offset = -1;

    public EntityQuery(JunifyDB db, Class<T> entityClass) {
        this.db = Objects.requireNonNull(db, "JunifyDB instance cannot be null");
        this.entityClass = Objects.requireNonNull(entityClass, "Entity class cannot be null");
        this.collectionName = EntityMapper.getCollectionName(entityClass);
    }

    /**
     * Sets the WHERE predicate with optional parameters.
     *
     * @param clause SQL WHERE clause (e.g. "age > ? AND status = ?")
     * @param params Positional parameter values
     */
    public EntityQuery<T> where(String clause, Object... params) {
        this.whereClause = clause;
        this.parameters.clear();
        if (params != null) {
            Collections.addAll(this.parameters, params);
        }
        return this;
    }

    /**
     * Orders the query results by the specified field ascending.
     */
    public EntityQuery<T> orderBy(String field) {
        return orderBy(field, true);
    }

    /**
     * Orders the query results by the specified field.
     */
    public EntityQuery<T> orderBy(String field, boolean ascending) {
        this.orderByField = field;
        this.ascending = ascending;
        return this;
    }

    /**
     * Limits the maximum number of entities returned.
     */
    public EntityQuery<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Offsets the query results.
     */
    public EntityQuery<T> offset(int offset) {
        this.offset = offset;
        return this;
    }

    /**
     * Executes the query and returns all matching entities as a List.
     */
    public List<T> list() {
        SqlResultSet rs = executeQuery();
        return rs.mapTo(entityClass);
    }

    /**
     * Returns a sequential Stream of matching entities.
     */
    public Stream<T> stream() {
        return list().stream();
    }

    /**
     * Executes the query and returns the first matching entity, or Optional.empty().
     */
    public Optional<T> first() {
        int originalLimit = this.limit;
        this.limit = 1;
        try {
            List<T> results = list();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            this.limit = originalLimit;
        }
    }

    /**
     * Counts the total number of matching entities.
     */
    public long count() {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS cnt FROM ").append(collectionName);
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(" WHERE ").append(whereClause.trim());
        }
        SqlResultSet rs = db.sql(sql.toString(), parameters.toArray());
        for (var row : rs) {
            Object cnt = row.get("cnt");
            if (cnt instanceof Number n) {
                return n.longValue();
            }
        }
        return 0;
    }

    private SqlResultSet executeQuery() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ").append(collectionName);
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql.append(" WHERE ").append(whereClause.trim());
        }
        if (orderByField != null && !orderByField.trim().isEmpty()) {
            sql.append(" ORDER BY ").append(orderByField.trim()).append(ascending ? " ASC" : " DESC");
        }
        if (limit > 0) {
            sql.append(" LIMIT ").append(limit);
        }
        if (offset > 0) {
            sql.append(" OFFSET ").append(offset);
        }
        return db.sql(sql.toString(), parameters.toArray());
    }
}
