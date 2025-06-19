package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BranchRepositoryTest {

    @Autowired
    private BranchRepository branchRepository;

    @Test
    void saveAndLoadBranchHierarchy() {
        Branch parent = new Branch("Parent", null);
        branchRepository.save(parent);

        Branch child = new Branch("Child", parent);
        branchRepository.save(child);

        assertTrue(branchRepository.existsById(parent.getId()));
        assertTrue(branchRepository.existsById(child.getId()));

        List<Branch> all = branchRepository.findAll();
        assertEquals(2, all.size());

        Branch foundChild = branchRepository.findById(child.getId()).orElseThrow();
        assertEquals("Child", foundChild.getName());
        assertNotNull(foundChild.getParent());
        assertEquals(parent.getId(), foundChild.getParent().getId());
    }
    @Test
    void findAllReturnsEmptyListWhenNoBranchesExist() {
        List<Branch> all = branchRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void findByIdReturnsEmptyOptionalForNonExistentBranch() {
        assertTrue(branchRepository.findById(999L).isEmpty());
    }

    @Test
    void existsByIdReturnsFalseForNonExistentBranch() {
        assertFalse(branchRepository.existsById(12345L));
    }

    @Test
    void saveBranchWithNullParentPersistsSuccessfully() {
        Branch branch = new Branch("Root", null);
        Branch saved = branchRepository.save(branch);
        assertNotNull(saved.getId());
        assertNull(saved.getParent());
        assertEquals("Root", saved.getName());
    }
}