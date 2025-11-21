package com.tablecraft.app.dynamic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlBasedTableService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Map<String, List<String>> tableColumns = new HashMap<>();
    private Set<String> availableTables = new HashSet<>();
    private Map<String, String> tablePrimaryKeyTypes = new HashMap<>(); // "single" or "composite"
    private Map<String, List<String>> compositeKeys = new HashMap<>();

    @PostConstruct
    public void initializeTables() {
        try {
            // SQLファイルの解析が複雑すぎるため、直接手動でテーブル作成
            createTablesManually();
        } catch (Exception e) {
            System.err.println("Error initializing tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String fixOrderDetailsTable(String createTableSql) {
        // order_detailsテーブルの重複PRIMARY KEY問題を修正
        return createTableSql.replaceAll(
                "order_id\\s+BIGINT\\s+NOT\\s+NULL\\s+PRIMARY\\s+KEY,\\s*product_id\\s+BIGINT\\s+NOT\\s+NULL\\s+PRIMARY\\s+KEY,",
                "order_id BIGINT NOT NULL, product_id BIGINT NOT NULL,");
    }

    private void createTablesManually() {
        try {
            // Users table
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS users (" +
                            "id BIGINT AUTO_INCREMENT, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "email VARCHAR(255) NOT NULL, " +
                            "age INT, " +
                            "phone VARCHAR(20), " +
                            "created_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (id))");
            availableTables.add("users");
            tableColumns.put("users", Arrays.asList("id", "name", "email", "age", "phone", "created_date"));
            tablePrimaryKeyTypes.put("users", "single");

            // Categories table
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS categories (" +
                            "id BIGINT AUTO_INCREMENT, " +
                            "name VARCHAR(100) NOT NULL, " +
                            "description LONGTEXT, " +
                            "sort_order INT DEFAULT 0, " +
                            "PRIMARY KEY (id))");
            availableTables.add("categories");
            tableColumns.put("categories", Arrays.asList("id", "name", "description", "sort_order"));
            tablePrimaryKeyTypes.put("categories", "single");

            // Products table
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS products (" +
                            "id BIGINT AUTO_INCREMENT, " +
                            "name VARCHAR(200) NOT NULL, " +
                            "category_id BIGINT, " +
                            "price DECIMAL(10,2) NOT NULL, " +
                            "stock INT DEFAULT 0, " +
                            "is_active BOOLEAN DEFAULT true, " +
                            "PRIMARY KEY (id))");
            availableTables.add("products");
            tableColumns.put("products", Arrays.asList("id", "name", "category_id", "price", "stock", "is_active"));
            tablePrimaryKeyTypes.put("products", "single");

            // Order details table (ordersテーブルなしでも動作するように)
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS order_details (" +
                            "order_id BIGINT NOT NULL, " +
                            "product_id BIGINT NOT NULL, " +
                            "quantity INT NOT NULL DEFAULT 1, " +
                            "unit_price DECIMAL(10,2) NOT NULL, " +
                            "PRIMARY KEY (order_id, product_id))");
            availableTables.add("order_details");
            tableColumns.put("order_details", Arrays.asList("order_id", "product_id", "quantity", "unit_price"));
            tablePrimaryKeyTypes.put("order_details", "composite");
            compositeKeys.put("order_details", Arrays.asList("order_id", "product_id"));

            // Test table with 3 composite keys
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS inventory_logs (" +
                            "warehouse_id BIGINT NOT NULL, " +
                            "product_id BIGINT NOT NULL, " +
                            "log_date DATE NOT NULL, " +
                            "quantity INT NOT NULL, " +
                            "operation_type VARCHAR(20) NOT NULL, " +
                            "notes VARCHAR(500), " +
                            "PRIMARY KEY (warehouse_id, product_id, log_date))");
            availableTables.add("inventory_logs");
            tableColumns.put("inventory_logs",
                    Arrays.asList("warehouse_id", "product_id", "log_date", "quantity", "operation_type", "notes"));
            tablePrimaryKeyTypes.put("inventory_logs", "composite");
            compositeKeys.put("inventory_logs", Arrays.asList("warehouse_id", "product_id", "log_date"));

            // Test table with 4 composite keys
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS sales_matrix (" +
                            "region_id INT NOT NULL, " +
                            "year_num INT NOT NULL, " +
                            "quarter_num INT NOT NULL, " +
                            "product_category_id INT NOT NULL, " +
                            "total_sales DECIMAL(15,2) NOT NULL, " +
                            "sales_count INT NOT NULL DEFAULT 0, " +
                            "PRIMARY KEY (region_id, year_num, quarter_num, product_category_id))");
            availableTables.add("sales_matrix");
            tableColumns.put("sales_matrix", Arrays.asList("region_id", "year_num", "quarter_num",
                    "product_category_id", "total_sales", "sales_count"));
            tablePrimaryKeyTypes.put("sales_matrix", "composite");
            compositeKeys.put("sales_matrix",
                    Arrays.asList("region_id", "year_num", "quarter_num", "product_category_id"));

            // Test table with 5 composite keys
            jdbcTemplate.execute(
                    "CREATE TABLE IF NOT EXISTS detailed_analytics (" +
                            "tenant_id INT NOT NULL, " +
                            "year_num INT NOT NULL, " +
                            "month_num INT NOT NULL, " +
                            "department_id INT NOT NULL, " +
                            "metric_type_id INT NOT NULL, " +
                            "metric_value DECIMAL(20,4) NOT NULL, " +
                            "calculation_method VARCHAR(50), " +
                            "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                            "PRIMARY KEY (tenant_id, year_num, month_num, department_id, metric_type_id))");
            availableTables.add("detailed_analytics");
            tableColumns.put("detailed_analytics", Arrays.asList("tenant_id", "year_num", "month_num", "department_id",
                    "metric_type_id", "metric_value", "calculation_method", "last_updated"));
            tablePrimaryKeyTypes.put("detailed_analytics", "composite");
            compositeKeys.put("detailed_analytics",
                    Arrays.asList("tenant_id", "year_num", "month_num", "department_id", "metric_type_id"));

            System.out.println("Created 7 tables manually (including 3 composite key test tables)");
        } catch (Exception e) {
            System.err.println("Error creating tables manually: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createIndexesAndConstraints(String sqlContent) {
        try {
            // 外部キー制約は無視して、インデックスのみ作成を試行
            if (availableTables.contains("products") && availableTables.contains("categories")) {
                try {
                    jdbcTemplate.execute("ALTER TABLE products ADD CONSTRAINT fk_products_category_id " +
                            "FOREIGN KEY (category_id) REFERENCES categories (id) ON DELETE SET NULL ON UPDATE CASCADE");
                } catch (Exception e) {
                    System.out.println("Foreign key constraint creation skipped: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Index/constraint creation completed with warnings");
        }
    }

    private List<String> extractColumnsFromCreate(String createTableSql) {
        return extractColumns(createTableSql);
    }

    private List<String> extractColumns(String createTableSql) {
        List<String> columns = new ArrayList<>();

        // CREATE TABLE文から列定義部分を抽出
        int startIndex = createTableSql.indexOf("(");
        int endIndex = createTableSql.lastIndexOf(")");

        if (startIndex != -1 && endIndex != -1) {
            String columnsSection = createTableSql.substring(startIndex + 1, endIndex);

            // 列定義を分割（コンマで区切られているが、関数内のコンマは無視）
            String[] columnDefs = columnsSection.split(",(?![^()]*\\))");

            for (String columnDef : columnDefs) {
                String trimmed = columnDef.trim();
                if (!trimmed.isEmpty() && !trimmed.toUpperCase().contains("PRIMARY KEY")
                        && !trimmed.toUpperCase().contains("FOREIGN KEY")
                        && !trimmed.toUpperCase().contains("CONSTRAINT")) {

                    // 列名を抽出（最初の単語）
                    String[] parts = trimmed.split("\\s+");
                    if (parts.length > 0) {
                        columns.add(parts[0]);
                    }
                }
            }
        }

        return columns;
    }

    public Set<String> getAvailableTables() {
        return availableTables;
    }

    public List<String> getTableColumns(String tableName) {
        return tableColumns.getOrDefault(tableName, new ArrayList<>());
    }

    // 主キータイプを取得
    public String getPrimaryKeyType(String tableName) {
        return tablePrimaryKeyTypes.getOrDefault(tableName, "single");
    }

    // 複合主キーのカラムを取得
    public List<String> getCompositeKeyColumns(String tableName) {
        return compositeKeys.getOrDefault(tableName, new ArrayList<>());
    }

    // 複合主キーでレコードを検索
    public Map<String, Object> findByCompositeKey(String tableName, Map<String, Object> keyValues) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        if (!"composite".equals(getPrimaryKeyType(tableName))) {
            throw new IllegalArgumentException("Table does not have composite primary key: " + tableName);
        }

        List<String> keyColumns = getCompositeKeyColumns(tableName);
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (String keyColumn : keyColumns) {
            if (keyValues.containsKey(keyColumn)) {
                conditions.add(keyColumn + " = ?");
                parameters.add(keyValues.get(keyColumn));
            } else {
                throw new IllegalArgumentException("Missing key value for: " + keyColumn);
            }
        }

        String sql = "SELECT * FROM " + tableName + " WHERE " + String.join(" AND ", conditions);

        try {
            return jdbcTemplate.queryForMap(sql, parameters.toArray());
        } catch (Exception e) {
            return null;
        }
    }

    // 複合主キーテーブル用のレコード作成
    public Map<String, Object> createComposite(String tableName, Map<String, Object> data) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        if (!"composite".equals(getPrimaryKeyType(tableName))) {
            throw new IllegalArgumentException("Table does not have composite primary key: " + tableName);
        }

        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            columns.add(entry.getKey());
            values.add(entry.getValue());
            placeholders.add("?");
        }

        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) +
                ") VALUES (" + String.join(", ", placeholders) + ")";

        try {
            jdbcTemplate.update(sql, values.toArray());
            return data; // 複合主キーでは自動生成IDがないので入力データを返す
        } catch (Exception e) {
            throw new RuntimeException("Error creating composite record: " + e.getMessage(), e);
        }
    }

    // 複合主キーテーブル用のレコード更新
    public Map<String, Object> updateComposite(String tableName, Map<String, Object> keyValues,
            Map<String, Object> data) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        if (!"composite".equals(getPrimaryKeyType(tableName))) {
            throw new IllegalArgumentException("Table does not have composite primary key: " + tableName);
        }

        List<String> keyColumns = getCompositeKeyColumns(tableName);
        List<String> setParts = new ArrayList<>();
        List<Object> updateValues = new ArrayList<>();
        List<Object> whereValues = new ArrayList<>();

        // SET部分を構築（主キー以外）
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!keyColumns.contains(entry.getKey())) {
                setParts.add(entry.getKey() + " = ?");
                updateValues.add(entry.getValue());
            }
        }

        // WHERE部分を構築
        List<String> whereConditions = new ArrayList<>();
        for (String keyColumn : keyColumns) {
            if (keyValues.containsKey(keyColumn)) {
                whereConditions.add(keyColumn + " = ?");
                whereValues.add(keyValues.get(keyColumn));
            } else {
                throw new IllegalArgumentException("Missing key value for: " + keyColumn);
            }
        }

        // 全パラメータを結合
        List<Object> allValues = new ArrayList<>(updateValues);
        allValues.addAll(whereValues);

        String sql = "UPDATE " + tableName + " SET " + String.join(", ", setParts) +
                " WHERE " + String.join(" AND ", whereConditions);

        try {
            int affectedRows = jdbcTemplate.update(sql, allValues.toArray());
            if (affectedRows > 0) {
                return findByCompositeKey(tableName, keyValues);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating composite record: " + e.getMessage(), e);
        }
    }

    // 複合主キーテーブル用のレコード削除
    public boolean deleteComposite(String tableName, Map<String, Object> keyValues) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        if (!"composite".equals(getPrimaryKeyType(tableName))) {
            throw new IllegalArgumentException("Table does not have composite primary key: " + tableName);
        }

        List<String> keyColumns = getCompositeKeyColumns(tableName);
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        for (String keyColumn : keyColumns) {
            if (keyValues.containsKey(keyColumn)) {
                conditions.add(keyColumn + " = ?");
                parameters.add(keyValues.get(keyColumn));
            } else {
                throw new IllegalArgumentException("Missing key value for: " + keyColumn);
            }
        }

        String sql = "DELETE FROM " + tableName + " WHERE " + String.join(" AND ", conditions);

        try {
            int affectedRows = jdbcTemplate.update(sql, parameters.toArray());
            return affectedRows > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting composite record: " + e.getMessage(), e);
        }
    }

    // CRUD操作メソッド（単一主キーおよび複合主キー対応）
    public Map<String, Object> create(String tableName, Map<String, Object> data) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        // 複合主キーの場合は専用メソッドを使用
        if ("composite".equals(getPrimaryKeyType(tableName))) {
            return createComposite(tableName, data);
        }

        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            // IDは自動生成のためスキップ（大文字小文字両方）
            if (!"id".equals(entry.getKey()) && !"ID".equals(entry.getKey())) {
                columns.add(entry.getKey());
                values.add(entry.getValue());
                placeholders.add("?");
            }
        }

        String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columns) +
                ") VALUES (" + String.join(", ", placeholders) + ")";

        try {
            // H2データベース対応: KeyHolderを使用して自動生成されたIDを取得
            org.springframework.jdbc.support.KeyHolder keyHolder = new org.springframework.jdbc.support.GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                java.sql.PreparedStatement ps = connection.prepareStatement(sql,
                        java.sql.Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < values.size(); i++) {
                    ps.setObject(i + 1, values.get(i));
                }
                return ps;
            }, keyHolder);

            // 生成されたIDを取得（H2データベース対応）
            Long generatedId = null;
            if (keyHolder.getKeys() != null && !keyHolder.getKeys().isEmpty()) {
                // H2では複数のキーが返される可能性があるため、IDまたはidキーを探す
                Map<String, Object> keys = keyHolder.getKeys();
                if (keys.containsKey("ID")) {
                    generatedId = ((Number) keys.get("ID")).longValue();
                } else if (keys.containsKey("id")) {
                    generatedId = ((Number) keys.get("id")).longValue();
                }
            } else if (keyHolder.getKey() != null) {
                // 単一キーの場合のフォールバック
                generatedId = keyHolder.getKey().longValue();
            }

            Map<String, Object> result = new HashMap<>(data);
            result.put("id", generatedId);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error creating record: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> findById(String tableName, Long id) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        String sql = "SELECT * FROM " + tableName + " WHERE id = ?";

        try {
            return jdbcTemplate.queryForMap(sql, id);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> findAll(String tableName) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        String sql;
        String primaryKeyType = getPrimaryKeyType(tableName);

        if ("composite".equals(primaryKeyType)) {
            // 複合主キーテーブルの場合、複合主キーの最初のカラムでソート
            List<String> keyColumns = getCompositeKeyColumns(tableName);
            if (keyColumns != null && !keyColumns.isEmpty()) {
                sql = "SELECT * FROM " + tableName + " ORDER BY " + keyColumns.get(0);
            } else {
                // フォールバック: ソートなし
                sql = "SELECT * FROM " + tableName;
            }
        } else {
            // 単一主キーテーブルの場合、従来通りidでソート
            sql = "SELECT * FROM " + tableName + " ORDER BY id";
        }

        try {
            return jdbcTemplate.queryForList(sql);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching records: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> update(String tableName, Long id, Map<String, Object> data) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        List<String> setParts = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            // IDは更新しない（大文字小文字両方）
            if (!"id".equals(entry.getKey()) && !"ID".equals(entry.getKey())) {
                setParts.add(entry.getKey() + " = ?");
                values.add(entry.getValue());
            }
        }

        values.add(id); // WHERE句のIDを追加

        String sql = "UPDATE " + tableName + " SET " + String.join(", ", setParts) + " WHERE id = ?";

        try {
            int affectedRows = jdbcTemplate.update(sql, values.toArray());
            if (affectedRows > 0) {
                return findById(tableName, id);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating record: " + e.getMessage(), e);
        }
    }

    public boolean delete(String tableName, Long id) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try {
            int affectedRows = jdbcTemplate.update(sql, id);
            return affectedRows > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting record: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getTableSchema(String tableName) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        try {
            String sql = "SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_DEFAULT " +
                    "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? ORDER BY ORDINAL_POSITION";

            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, tableName.toUpperCase());

            Map<String, Object> schema = new HashMap<>();
            schema.put("tableName", tableName);
            schema.put("columns", columns);

            return schema;
        } catch (Exception e) {
            throw new RuntimeException("Error getting table schema: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> search(String tableName, Map<String, Object> searchCriteria,
            Integer page, Integer size, String sortBy, String sortOrder) {
        if (!availableTables.contains(tableName)) {
            throw new IllegalArgumentException("Table not found: " + tableName);
        }

        try {
            // Base SQL
            StringBuilder sql = new StringBuilder("SELECT * FROM " + tableName);
            List<Object> parameters = new ArrayList<>();

            // WHERE clause for search criteria
            if (searchCriteria != null && !searchCriteria.isEmpty()) {
                sql.append(" WHERE ");
                List<String> conditions = new ArrayList<>();

                for (Map.Entry<String, Object> entry : searchCriteria.entrySet()) {
                    String column = entry.getKey();
                    Object value = entry.getValue();

                    if (value != null && !value.toString().isEmpty()) {
                        // Text search with LIKE
                        if (value instanceof String) {
                            conditions.add(column + " LIKE ?");
                            parameters.add("%" + value + "%");
                        } else {
                            // Exact match for numbers, dates, etc.
                            conditions.add(column + " = ?");
                            parameters.add(value);
                        }
                    }
                }
                sql.append(String.join(" AND ", conditions));
            }

            // ORDER BY clause
            if (sortBy != null && !sortBy.isEmpty()) {
                sql.append(" ORDER BY ").append(sortBy);
                if ("desc".equalsIgnoreCase(sortOrder)) {
                    sql.append(" DESC");
                } else {
                    sql.append(" ASC");
                }
            } else {
                sql.append(" ORDER BY id ASC"); // Default sort
            }

            // Count total records for pagination
            String countSql = sql.toString().replace("SELECT *", "SELECT COUNT(*)");
            Integer totalCountResult = jdbcTemplate.queryForObject(countSql, parameters.toArray(), Integer.class);
            int totalCount = totalCountResult != null ? totalCountResult : 0;

            // LIMIT and OFFSET for pagination
            if (page != null && size != null && size > 0) {
                int offset = page * size;
                sql.append(" LIMIT ? OFFSET ?");
                parameters.add(size);
                parameters.add(offset);
            }

            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql.toString(), parameters.toArray());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", data);
            result.put("totalCount", totalCount);
            result.put("page", page != null ? page : 0);
            result.put("size", size != null ? size : data.size());
            result.put("totalPages", size != null && size > 0 ? (totalCount + size - 1) / size : 1);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Error searching records: " + e.getMessage(), e);
        }
    }
}