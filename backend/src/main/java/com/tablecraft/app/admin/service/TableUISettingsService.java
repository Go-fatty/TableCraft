package com.tablecraft.app.admin.service;

import com.tablecraft.app.admin.entity.TableUISettings;
import com.tablecraft.app.admin.repository.TableUISettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing table UI settings
 */
@Service
public class TableUISettingsService {

    @Autowired
    private TableUISettingsRepository repository;

    @Autowired
    private TableSchemaService tableSchemaService;

    /**
     * Get all UI settings
     * @return List of all UI settings
     */
    public List<TableUISettings> getAllSettings() {
        return repository.findAll();
    }

    /**
     * Get UI settings by table name
     * @param tableName Table name
     * @return UI settings or empty if not found
     */
    public Optional<TableUISettings> getSettingsByTableName(String tableName) {
        return repository.findByTableName(tableName);
    }

    /**
     * Get UI settings by ID
     * @param id Settings ID
     * @return UI settings or empty if not found
     */
    public Optional<TableUISettings> getSettingsById(Long id) {
        return repository.findById(id);
    }

    /**
     * Create or update UI settings
     * @param settings UI settings to save
     * @return Saved UI settings
     * @throws IllegalArgumentException if table doesn't exist in database
     */
    @Transactional
    public TableUISettings saveSettings(TableUISettings settings) {
        // Verify that the table exists in the database
        if (!tableSchemaService.tableExists(settings.getTableName())) {
            throw new IllegalArgumentException("Table does not exist: " + settings.getTableName());
        }

        return repository.save(settings);
    }

    /**
     * Create UI settings with default values
     * @param tableName Table name
     * @param displayName Display name
     * @return Created UI settings
     * @throws IllegalArgumentException if table doesn't exist in database
     */
    @Transactional
    public TableUISettings createDefaultSettings(String tableName, String displayName) {
        // Verify that the table exists in the database
        if (!tableSchemaService.tableExists(tableName)) {
            throw new IllegalArgumentException("Table does not exist: " + tableName);
        }

        // Check if settings already exist
        if (repository.existsByTableName(tableName)) {
            throw new IllegalArgumentException("UI settings already exist for table: " + tableName);
        }

        TableUISettings settings = new TableUISettings();
        settings.setTableName(tableName);
        settings.setDisplayName(displayName);
        // Default values are set in the entity constructor

        return repository.save(settings);
    }

    /**
     * Update UI settings
     * @param id Settings ID
     * @param settings Updated settings
     * @return Updated UI settings
     * @throws IllegalArgumentException if settings not found or table doesn't exist
     */
    @Transactional
    public TableUISettings updateSettings(Long id, TableUISettings settings) {
        TableUISettings existing = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("UI settings not found: " + id));

        // Verify that the table exists in the database
        if (!tableSchemaService.tableExists(settings.getTableName())) {
            throw new IllegalArgumentException("Table does not exist: " + settings.getTableName());
        }

        // Update fields
        existing.setTableName(settings.getTableName());
        existing.setDisplayName(settings.getDisplayName());
        existing.setDescription(settings.getDescription());
        existing.setEnableSearch(settings.getEnableSearch());
        existing.setEnableSort(settings.getEnableSort());
        existing.setEnablePagination(settings.getEnablePagination());
        existing.setPageSize(settings.getPageSize());
        existing.setAllowCreate(settings.getAllowCreate());
        existing.setAllowEdit(settings.getAllowEdit());
        existing.setAllowDelete(settings.getAllowDelete());
        existing.setAllowBulk(settings.getAllowBulk());

        return repository.save(existing);
    }

    /**
     * Delete UI settings by ID
     * @param id Settings ID
     * @throws IllegalArgumentException if settings not found
     */
    @Transactional
    public void deleteSettings(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("UI settings not found: " + id);
        }
        repository.deleteById(id);
    }

    /**
     * Delete UI settings by table name
     * @param tableName Table name
     */
    @Transactional
    public void deleteSettingsByTableName(String tableName) {
        repository.deleteByTableName(tableName);
    }

    /**
     * Check if UI settings exist for a table
     * @param tableName Table name
     * @return true if settings exist
     */
    public boolean existsByTableName(String tableName) {
        return repository.existsByTableName(tableName);
    }
}
