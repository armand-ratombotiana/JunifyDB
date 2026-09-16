package org.junify.db.jpa;

import jakarta.persistence.*;
import org.junify.db.JunifyDB;
import org.junify.db.sql.SqlResultSet;

import java.util.*;
import java.util.stream.Stream;

public class JunifyTypedQuery<X> implements TypedQuery<X> {

    private final JunifyDB db;
    private final String queryString;
    private final Class<X> resultClass;
    private final Map<Integer, Object> positionalParams = new HashMap<>();
    private final Map<String, Object> namedParams = new HashMap<>();
    private int maxResults = Integer.MAX_VALUE;
    private int firstResult = 0;
    private FlushModeType flushMode = FlushModeType.AUTO;

    public JunifyTypedQuery(JunifyDB db, String queryString, Class<X> resultClass) {
        this.db = db;
        this.queryString = queryString;
        this.resultClass = resultClass;
    }

    @Override
    public List<X> getResultList() {
        SqlResultSet rs = executeInternal();
        if (resultClass != null && !resultClass.equals(Object[].class) && !resultClass.equals(Object.class)) {
            return rs.mapTo(resultClass);
        }
        List<X> list = new ArrayList<>();
        for (var row : rs) {
            list.add((X) row.asMap());
        }
        return list;
    }

    @Override
    public X getSingleResult() {
        List<X> list = getResultList();
        if (list.isEmpty()) {
            throw new NoResultException("No entity found for query: " + queryString);
        }
        if (list.size() > 1) {
            throw new NonUniqueResultException("Multiple entities found for query: " + queryString);
        }
        return list.get(0);
    }

    @Override
    public Stream<X> getResultStream() {
        return getResultList().stream();
    }

    @Override
    public int executeUpdate() {
        SqlResultSet rs = executeInternal();
        return rs.getUpdateCount();
    }

    private SqlResultSet executeInternal() {
        String finalSql = queryString;
        List<Object> params = new ArrayList<>();

        // Handle positional parameters in order (1-based in JPA or 0-based)
        if (!positionalParams.isEmpty()) {
            int maxIdx = Collections.max(positionalParams.keySet());
            int startIdx = positionalParams.containsKey(1) ? 1 : 0;
            for (int i = startIdx; i <= maxIdx; i++) {
                params.add(positionalParams.get(i));
            }
        }

        // Replace named parameters (:name) with ? in order of appearance in finalSql
        if (!namedParams.isEmpty()) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(":([a-zA-Z0-9_]+)");
            java.util.regex.Matcher m = p.matcher(finalSql);
            StringBuilder sb = new StringBuilder();
            while (m.find()) {
                String name = m.group(1);
                if (namedParams.containsKey(name)) {
                    params.add(namedParams.get(name));
                    m.appendReplacement(sb, "?");
                }
            }
            m.appendTail(sb);
            finalSql = sb.toString();
        }

        // Append LIMIT / OFFSET if not already present
        if (maxResults < Integer.MAX_VALUE && !finalSql.toUpperCase().contains("LIMIT")) {
            finalSql += " LIMIT " + maxResults;
            if (firstResult > 0 && !finalSql.toUpperCase().contains("OFFSET")) {
                finalSql += " OFFSET " + firstResult;
            }
        }

        return db.sql(finalSql, params.toArray());
    }

    @Override
    public TypedQuery<X> setMaxResults(int maxResult) {
        this.maxResults = maxResult;
        return this;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public TypedQuery<X> setFirstResult(int startPosition) {
        this.firstResult = startPosition;
        return this;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
    }

    @Override
    public TypedQuery<X> setHint(String hintName, Object value) {
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return Collections.emptyMap();
    }

    @Override
    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        if (param.getName() != null) {
            namedParams.put(param.getName(), value);
        } else if (param.getPosition() != null) {
            positionalParams.put(param.getPosition(), value);
        }
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        return setParameter(param, value);
    }

    @Override
    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        return setParameter(param, value);
    }

    @Override
    public TypedQuery<X> setParameter(String name, Object value) {
        namedParams.put(name, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        return setParameter(name, value);
    }

    @Override
    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
        return setParameter(name, value);
    }

    @Override
    public TypedQuery<X> setParameter(int position, Object value) {
        positionalParams.put(position, value);
        return this;
    }

    @Override
    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        return setParameter(position, value);
    }

    @Override
    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        return setParameter(position, value);
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return Collections.emptySet();
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return null;
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return null;
    }

    @Override
    public Parameter<?> getParameter(int position) {
        return null;
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return null;
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        return true;
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        return null;
    }

    @Override
    public Object getParameterValue(String name) {
        return namedParams.get(name);
    }

    @Override
    public Object getParameterValue(int position) {
        return positionalParams.get(position);
    }

    @Override
    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return flushMode;
    }

    @Override
    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        return LockModeType.NONE;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        if (cls.isInstance(this)) return cls.cast(this);
        throw new PersistenceException("Cannot unwrap to " + cls.getName());
    }
}
