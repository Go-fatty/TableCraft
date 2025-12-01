package com.tablecraft.app.admin.service;

import com.tablecraft.app.admin.dto.TableSchemaInfo;
import com.tablecraft.app.admin.dto.TableSchemaInfo.ColumnSchemaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for retrieving table schema information from INFORMATION_SCHEMA
 */
@Service
public class TableSchemaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Get all tables in the current database
     * @return List of table names
     */
    public List<String> getAllTableNames() {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                     "WHERE TABLE_SCHEMA = DATABASE() " +
                     "AND TABLE_TYPE = 'BASE TABLE' " +
                     "ORDER BY TABLE_NAME";
        
        return jdbcTemplate.queryForList(sql, String.class);
    }

    /**
     * Get detailed schema information for a specific table
     * @param tableName Table name
     * @return Table schema information
     */
    public Optional<TableSchemaInfo> getTableSchema(String tableName) {
        // Get table information
        String tableSql = "SELECT TABLE_NAME, TABLE_SCHEMA, TABLE_COMMENT " +
                         "FROM INFORMATION_SCHEMA.TABLES " +
                         "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        
        List<TableSchemaInfo> tables = jdbcTemplate.query(tableSql, 
            new Object[]{tableName},
            (rs, rowNum) -> {
                TableSchemaInfo info = new TableSchemaInfo();
                info.setTableName(rs.getString("TABLE_NAME"));
                info.setTableSchema(rs.getString("TABLE_SCHEMA"));
                info.setTableComment(rs.getString("TABLE_COMMENT"));
                return info;
            }
        );

        if (tables.isEmpty()) {
            return Optional.empty();
        }

        TableSchemaInfo tableInfo = tables.get(0);

        // Get column information
        List<ColumnSchemaInfo> columns = getTableColumns(tableName);
        tableInfo.setColumns(columns);

        return Optional.of(tableInfo);
    }

    /**
     * Get column information for a specific table
     * @param tableName Table name
     * @return List of column information
     */
    public List<ColumnSchemaInfo> getTableColumns(String tableName) {
        String sql = "SELECT " +
                     "COLUMN_NAME, DATA_TYPE, COLUMN_TYPE, " +
                     "IS_NULLABLE, COLUMN_KEY, COLUMN_DEFAULT, " +
                     "EXTRA, COLUMN_COMMENT, " +
                     "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE " +
                     "FROM INFORMATION_SCHEMA.COLUMNS " +
                     "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? " +
                     "ORDER BY ORDINAL_POSITION";

        return jdbcTemplate.query(sql, 
            new Object[]{tableName},
            (rs, rowNum) -> {
                ColumnSchemaInfo column = new ColumnSchemaInfo();
                column.setColumnName(rs.getString("COLUMN_NAME"));
                column.setDataType(rs.getString("DATA_TYPE"));
                column.setColumnType(rs.getString("COLUMN_TYPE"));
                column.setIsNullable("YES".equals(rs.getString("IS_NULLABLE")));
                column.setColumnKey(rs.getString("COLUMN_KEY"));
                column.setColumnDefault(rs.getString("COLUMN_DEFAULT"));
                column.setExtra(rs.getString("EXTRA"));
                column.setColumnComment(rs.getString("COLUMN_COMMENT"));
                
                Object charMaxLength = rs.getObject("CHARACTER_MAXIMUM_LENGTH");
                column.setCharacterMaximumLength(charMaxLength != null ? ((Number) charMaxLength).intValue() : null);
                
                Object numPrecision = rs.getObject("NUMERIC_PRECISION");
                column.setNumericPrecision(numPrecision != null ? ((Number) numPrecision).intValue() : null);
                
                Object numScale = rs.getObject("NUMERIC_SCALE");
                column.setNumericScale(numScale != null ? ((Number) numScale).intValue() : null);
                
                return column;
            }
        );
    }

    /**
     * Check if a table exists in the database
     * @param tableName Table name
     * @return true if table exists
     */
    public boolean tableExists(String tableName) {
        String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                     "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?";
        
        Integer count = jdbcTemplate.queryForObject(sql, new Object[]{tableName}, Integer.class);
        return count != null && count > 0;
    }
}
