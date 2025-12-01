package com.tablecraft.app.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tablecraft.app.admin.entity.ParsedTableDefinition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQLパーサーサービス
 * CREATE TABLE文を解析してテーブル定義を抽出
 */
@Service
public class SqlParserService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * SQL文からCREATE TABLE定義をすべて解析
     */
    public List<ParsedTableDefinition> parseAllTables(String sqlContent, Long sqlFileId) {
        List<ParsedTableDefinition> definitions = new ArrayList<>();

        // CREATE TABLE文を抽出（複数対応）
        Pattern createTablePattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?([`\"\\[]?\\w+[`\"\\]]?(?:\\.[`\"\\[]?\\w+[`\"\\]]?)?)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        Matcher matcher = createTablePattern.matcher(sqlContent);
        int lastEnd = 0;

        while (matcher.find(lastEnd)) {
            int start = matcher.start();
            String tableName = matcher.group(1);

            // テーブル定義の終端を検索（次のCREATE TABLEまたは末尾）
            Matcher nextMatcher = createTablePattern.matcher(sqlContent);
            int end = sqlContent.length();
            if (nextMatcher.find(matcher.end())) {
                end = nextMatcher.start();
            }

            String tableDefinition = sqlContent.substring(start, end).trim();

            try {
                ParsedTableDefinition definition = parseTableDefinition(tableDefinition, tableName, sqlFileId);
                definitions.add(definition);
            } catch (Exception e) {
                // パースエラーは無視して次へ
                System.err.println("Failed to parse table: " + tableName + ", error: " + e.getMessage());
            }

            lastEnd = end;
        }

        return definitions;
    }

    /**
     * 単一テーブルのCREATE TABLE文を解析
     */
    public ParsedTableDefinition parseTableDefinition(String sql, String fullTableName, Long sqlFileId) {
        ParsedTableDefinition definition = new ParsedTableDefinition();
        definition.setSqlFileId(sqlFileId);

        // スキーマ名とテーブル名を分離
        String[] parts = fullTableName.replaceAll("[`\"\\[\\]]", "").split("\\.");
        if (parts.length == 2) {
            definition.setSchemaName(parts[0]);
            definition.setTableName(parts[1]);
        } else {
            definition.setTableName(parts[0]);
        }

        // テーブル構造を解析
        Map<String, Object> tableStructure = parseTableStructure(sql);
        definition.setTableStructure(convertToJson(tableStructure));

        // 外部キーを解析
        List<Map<String, String>> foreignKeys = parseForeignKeys(sql);
        definition.setForeignKeys(convertToJson(foreignKeys));

        // インデックスを解析
        List<Map<String, Object>> indexes = parseIndexes(sql);
        definition.setIndexes(convertToJson(indexes));

        return definition;
    }

    /**
     * テーブル構造を解析（カラム定義）
     */
    private Map<String, Object> parseTableStructure(String sql) {
        Map<String, Object> structure = new HashMap<>();
        List<Map<String, Object>> columns = new ArrayList<>();

        // CREATE TABLE ... ( の中身を抽出
        Pattern bodyPattern = Pattern.compile("\\(([^)]+(?:\\([^)]*\\)[^)]*)*)\\)", Pattern.DOTALL);
        Matcher bodyMatcher = bodyPattern.matcher(sql);

        if (bodyMatcher.find()) {
            String body = bodyMatcher.group(1);

            // カラム定義を行ごとに分割
            String[] lines = body.split(",(?![^()]*\\))");

            for (String line : lines) {
                line = line.trim();

                // CONSTRAINTやKEYで始まる行はスキップ
                if (line.matches("(?i)^(CONSTRAINT|PRIMARY\\s+KEY|FOREIGN\\s+KEY|KEY|INDEX|UNIQUE).*")) {
                    continue;
                }

                Map<String, Object> column = parseColumnDefinition(line);
                if (!column.isEmpty()) {
                    columns.add(column);
                }
            }
        }

        structure.put("columns", columns);
        return structure;
    }

    /**
     * 単一カラムの定義を解析
     */
    private Map<String, Object> parseColumnDefinition(String line) {
        Map<String, Object> column = new HashMap<>();

        // カラム名を抽出（バッククォート・ダブルクォート対応）
        Pattern columnPattern = Pattern.compile("^([`\"\\[]?\\w+[`\"\\]]?)\\s+([^,]+)");
        Matcher matcher = columnPattern.matcher(line);

        if (!matcher.find()) {
            return column;
        }

        String columnName = matcher.group(1).replaceAll("[`\"\\[\\]]", "");
        String definition = matcher.group(2).trim();

        column.put("name", columnName);

        // データ型を解析
        Pattern typePattern = Pattern.compile("^(\\w+)(?:\\((\\d+(?:,\\s*\\d+)?)\\))?");
        Matcher typeMatcher = typePattern.matcher(definition);

        if (typeMatcher.find()) {
            column.put("type", typeMatcher.group(1).toUpperCase());
            if (typeMatcher.group(2) != null) {
                column.put("length", typeMatcher.group(2));
            }
        }

        // 制約を解析
        column.put("nullable", !line.matches("(?i).*NOT\\s+NULL.*"));
        column.put("primaryKey", line.matches("(?i).*PRIMARY\\s+KEY.*"));
        column.put("autoIncrement", line.matches("(?i).*(AUTO_INCREMENT|IDENTITY).*"));
        column.put("unique", line.matches("(?i).*UNIQUE.*"));

        // DEFAULT値を解析
        Pattern defaultPattern = Pattern.compile("(?i)DEFAULT\\s+([^,\\s]+(?:\\s+[^,\\s]+)*)");
        Matcher defaultMatcher = defaultPattern.matcher(line);
        if (defaultMatcher.find()) {
            column.put("defaultValue", defaultMatcher.group(1).trim());
        }

        // COMMENT を解析
        Pattern commentPattern = Pattern.compile("(?i)COMMENT\\s+'([^']*)'");
        Matcher commentMatcher = commentPattern.matcher(line);
        if (commentMatcher.find()) {
            column.put("comment", commentMatcher.group(1));
        }

        return column;
    }

    /**
     * 外部キー制約を解析
     */
    private List<Map<String, String>> parseForeignKeys(String sql) {
        List<Map<String, String>> foreignKeys = new ArrayList<>();

        Pattern fkPattern = Pattern.compile(
                "(?i)(?:CONSTRAINT\\s+([`\"\\[]?\\w+[`\"\\]]?)\\s+)?FOREIGN\\s+KEY\\s*\\(([^)]+)\\)\\s*REFERENCES\\s+([`\"\\[]?\\w+[`\"\\]]?)\\s*\\(([^)]+)\\)(?:\\s+ON\\s+DELETE\\s+(CASCADE|SET\\s+NULL|NO\\s+ACTION|RESTRICT))?(?:\\s+ON\\s+UPDATE\\s+(CASCADE|SET\\s+NULL|NO\\s+ACTION|RESTRICT))?",
                Pattern.DOTALL);

        Matcher matcher = fkPattern.matcher(sql);

        while (matcher.find()) {
            Map<String, String> fk = new HashMap<>();
            if (matcher.group(1) != null) {
                fk.put("constraintName", matcher.group(1).replaceAll("[`\"\\[\\]]", ""));
            }
            fk.put("columns", matcher.group(2).replaceAll("[`\"\\[\\]\\s]", ""));
            fk.put("referencedTable", matcher.group(3).replaceAll("[`\"\\[\\]]", ""));
            fk.put("referencedColumns", matcher.group(4).replaceAll("[`\"\\[\\]\\s]", ""));
            if (matcher.group(5) != null) {
                fk.put("onDelete", matcher.group(5));
            }
            if (matcher.group(6) != null) {
                fk.put("onUpdate", matcher.group(6));
            }
            foreignKeys.add(fk);
        }

        return foreignKeys;
    }

    /**
     * インデックスを解析
     */
    private List<Map<String, Object>> parseIndexes(String sql) {
        List<Map<String, Object>> indexes = new ArrayList<>();

        // PRIMARY KEY
        Pattern pkPattern = Pattern.compile("(?i)PRIMARY\\s+KEY\\s*\\(([^)]+)\\)");
        Matcher pkMatcher = pkPattern.matcher(sql);
        if (pkMatcher.find()) {
            Map<String, Object> pk = new HashMap<>();
            pk.put("name", "PRIMARY");
            pk.put("type", "PRIMARY_KEY");
            pk.put("columns", Arrays.asList(pkMatcher.group(1).replaceAll("[`\"\\[\\]\\s]", "").split(",")));
            indexes.add(pk);
        }

        // UNIQUE KEY
        Pattern uniquePattern = Pattern.compile(
                "(?i)(?:CONSTRAINT\\s+([`\"\\[]?\\w+[`\"\\]]?)\\s+)?UNIQUE(?:\\s+KEY)?\\s*(?:([`\"\\[]?\\w+[`\"\\]]?)\\s*)?\\(([^)]+)\\)");
        Matcher uniqueMatcher = uniquePattern.matcher(sql);
        while (uniqueMatcher.find()) {
            Map<String, Object> unique = new HashMap<>();
            String name = uniqueMatcher.group(1) != null ? uniqueMatcher.group(1) : uniqueMatcher.group(2);
            if (name != null) {
                unique.put("name", name.replaceAll("[`\"\\[\\]]", ""));
            }
            unique.put("type", "UNIQUE");
            unique.put("columns", Arrays.asList(uniqueMatcher.group(3).replaceAll("[`\"\\[\\]\\s]", "").split(",")));
            indexes.add(unique);
        }

        // INDEX/KEY
        Pattern indexPattern = Pattern.compile("(?i)(?:INDEX|KEY)\\s+([`\"\\[]?\\w+[`\"\\]]?)\\s*\\(([^)]+)\\)");
        Matcher indexMatcher = indexPattern.matcher(sql);
        while (indexMatcher.find()) {
            Map<String, Object> index = new HashMap<>();
            index.put("name", indexMatcher.group(1).replaceAll("[`\"\\[\\]]", ""));
            index.put("type", "INDEX");
            index.put("columns", Arrays.asList(indexMatcher.group(2).replaceAll("[`\"\\[\\]\\s]", "").split(",")));
            indexes.add(index);
        }

        return indexes;
    }

    /**
     * オブジェクトをJSON文字列に変換
     */
    private String convertToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
