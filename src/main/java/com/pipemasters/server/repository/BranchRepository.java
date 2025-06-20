package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;

public interface BranchRepository extends GeneralRepository<Branch, Long> {
    boolean existsByName(String name);
}
