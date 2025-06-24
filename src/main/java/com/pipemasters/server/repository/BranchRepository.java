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
}
