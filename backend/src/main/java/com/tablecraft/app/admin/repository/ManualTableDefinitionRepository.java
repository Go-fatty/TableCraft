package com.tablecraft.app.admin.repository;

import com.tablecraft.app.admin.entity.ManualTableDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ManualTableDefinitionRepository extends JpaRepository<ManualTableDefinition, Long> {

    Optional<ManualTableDefinition> findByTableName(String tableName);

    boolean existsByTableName(String tableName);
}
