package com.vaadin.devrel.featuretour.showcase.ai;

import com.vaadin.flow.component.ai.provider.DatabaseProvider;
import com.vaadin.flow.component.ai.provider.ToolException;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lets the grid and chart controllers query the sales table. The queries are
 * written by the LLM, so they run as a read-only database user.
 */
class JdbcDatabaseProvider implements DatabaseProvider {

    private final transient DataSource readOnlyDataSource;

    JdbcDatabaseProvider(DataSource readOnlyDataSource) {
        this.readOnlyDataSource = readOnlyDataSource;
    }

    @Override
    public String getSchema() {
        return SalesDatabase.SCHEMA;
    }

    @Override
    public List<Map<String, Object>> executeQuery(String sql) {
        try (var connection = readOnlyDataSource.getConnection();
                var statement = connection.prepareStatement(sql);
                var resultSet = statement.executeQuery()) {
            var meta = resultSet.getMetaData();
            var rows = new ArrayList<Map<String, Object>>();
            while (resultSet.next()) {
                var row = new LinkedHashMap<String, Object>();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    row.put(meta.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
            }
            return rows;
        } catch (SQLException e) {
            // New in 25.3: the message of a ToolException is relayed to the LLM,
            // so it can fix its query. H2 messages describe the SQL problem only.
            throw new ToolException("Query failed: " + e.getMessage(), e);
        }
    }
}
