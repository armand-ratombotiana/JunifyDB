package org.junify.db.sql;

import org.junify.db.adapter.jnosql.EntityMapper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Result set returned by SQL execution against JunifyDB.
 */
public class SqlResultSet implements Iterable<SqlRow> {

    private final List<SqlRow> rows;
    private final List<String> columnNames;
    private final int updateCount;
    private final String statementType;

    public SqlResultSet(List<SqlRow> rows, List<String> columnNames, int updateCount, String statementType) {
        this.rows = new ArrayList<>(rows);
        this.columnNames = new ArrayList<>(columnNames);
        this.updateCount = updateCount;
        this.statementType = statementType;
    }

    public static SqlResultSet ofRows(List<SqlRow> rows, List<String> columnNames) {
        return new SqlResultSet(rows, columnNames, rows.size(), "SELECT");
    }

    public static SqlResultSet ofUpdate(int updateCount, String statementType) {
        return new SqlResultSet(Collections.emptyList(), Collections.emptyList(), updateCount, statementType);
    }

    public int size() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public String getStatementType() {
        return statementType;
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public List<SqlRow> getRows() {
        return Collections.unmodifiableList(rows);
    }

    public SqlRow first() {
        return rows.isEmpty() ? null : rows.get(0);
    }

    public Optional<SqlRow> findFirst() {
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    @Override
    public Iterator<SqlRow> iterator() {
        return rows.iterator();
    }

    public Stream<SqlRow> stream() {
        return rows.stream();
    }

    /**
     * Maps the result rows to the specified entity class using EntityMapper.
     */
    public <T> List<T> mapTo(Class<T> entityClass) {
        return rows.stream()
                .map(row -> EntityMapper.fromDocument(row.asDocument(), entityClass))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "SqlResultSet[type=" + statementType + ", rows=" + rows.size() + ", updated=" + updateCount + "]";
    }
}
