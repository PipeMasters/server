package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BranchRepository extends GeneralRepository<Branch, Long> {
    boolean existsByName(String name);
    Optional<Branch> findByName(String name);
    @Query("select b from Branch b where b.parent.id = :parentId")
    List<Branch> findByParentId(Long parentId);
    List<Branch> findByParentIsNull();
    @Query(value = """
        WITH RECURSIVE branch_hierarchy (id, parent_id, level) AS (
            SELECT b.id, b.parent_id, 0
            FROM branches b
            WHERE b.parent_id IS NULL
            UNION ALL
            SELECT b_child.id, b_child.parent_id, bh.level + 1
            FROM branches b_child
            INNER JOIN branch_hierarchy bh ON b_child.parent_id = bh.id
        )
        SELECT b.*
        FROM branches b
        JOIN branch_hierarchy bh ON b.id = bh.id
        WHERE bh.level = :level
        """, nativeQuery = true)
    List<Branch> findByLevel(int level);
}
