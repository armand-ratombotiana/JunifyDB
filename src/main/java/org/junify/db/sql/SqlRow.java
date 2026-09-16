package org.junify.db.sql;

import org.junify.db.nosql.document.Document;

import java.time.Instant;
import java.util.*;

/**
 * Represents a single row resulting from an SQL query execution.
 */
public class SqlRow {

    private final Map<String, Object> data;
    private final List<String> columnNames;

    public SqlRow(Map<String, Object> data, List<String> columnNames) {
        this.data = new LinkedHashMap<>(data);
        this.columnNames = new ArrayList<>(columnNames);
    }

    public Object getObject(String column) {
        if (data.containsKey(column)) {
            return data.get(column);
        }
        for (Map.Entry<String, Object> e : data.entrySet()) {
            if (e.getKey().equalsIgnoreCase(column)) {
                return e.getValue();
            }
        }
        // Match qualified/unqualified variants (e.g. i.status matches status, or vice-versa)
        for (Map.Entry<String, Object> e : data.entrySet()) {
            String key = e.getKey();
            if (key.contains(".") && key.substring(key.lastIndexOf('.') + 1).equalsIgnoreCase(column)) {
                return e.getValue();
            }
            if (column.contains(".") && column.substring(column.lastIndexOf('.') + 1).equalsIgnoreCase(key)) {
                return e.getValue();
            }
        }
        return null;
    }

    public Object get(String column) {
        return getObject(column);
    }

    public Object getObject(int columnIndex) {
        if (columnIndex >= 0 && columnIndex < columnNames.size()) {
            return getObject(columnNames.get(columnIndex));
        }
        return null;
    }

    public String getString(String column) {
        Object val = getObject(column);
        return val != null ? val.toString() : null;
    }

    public Integer getInt(String column) {
        Object val = getObject(column);
        if (val instanceof Number n) return n.intValue();
        if (val != null) {
            try { return Integer.parseInt(val.toString()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Long getLong(String column) {
        Object val = getObject(column);
        if (val instanceof Number n) return n.longValue();
        if (val != null) {
            try { return Long.parseLong(val.toString()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Double getDouble(String column) {
        Object val = getObject(column);
        if (val instanceof Number n) return n.doubleValue();
        if (val != null) {
            try { return Double.parseDouble(val.toString()); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    public Boolean getBoolean(String column) {
        Object val = getObject(column);
        if (val instanceof Boolean b) return b;
        if (val != null) return Boolean.parseBoolean(val.toString());
        return null;
    }

    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(data);
    }

    public Document asDocument() {
        Document doc = new Document();
        Object id = getObject("id");
        if (id != null) doc.id(id.toString());
        data.forEach(doc::add);
        return doc;
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    @Override
    public String toString() {
        return "SqlRow" + data;
    }
}
