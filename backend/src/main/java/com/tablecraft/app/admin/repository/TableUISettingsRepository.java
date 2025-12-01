package com.tablecraft.app.admin.repository;

import com.tablecraft.app.admin.entity.TableUISettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for TableUISettings
 */
@Repository
public interface TableUISettingsRepository extends JpaRepository<TableUISettings, Long> {

    /**
     * Find UI settings by table name
     * @param tableName Table name
     * @return UI settings
     */
    Optional<TableUISettings> findByTableName(String tableName);

    /**
     * Check if UI settings exist for a table
     * @param tableName Table name
     * @return true if exists
     */
    boolean existsByTableName(String tableName);

    /**
     * Delete UI settings by table name
     * @param tableName Table name
     */
    void deleteByTableName(String tableName);
}
