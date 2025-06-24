package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

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

    @Test
    void findByNameReturnsBranchWhenExists() {
        Branch branch = branchRepository.save(new Branch("Unique Branch Name", null));
        Optional<Branch> found = branchRepository.findByName("Unique Branch Name");
        assertTrue(found.isPresent());
        assertEquals(branch.getId(), found.get().getId());
        assertEquals("Unique Branch Name", found.get().getName());
    }

    @Test
    void findByNameReturnsEmptyOptionalWhenNotExists() {
        Optional<Branch> found = branchRepository.findByName("NonExistent Branch");
        assertTrue(found.isEmpty());
    }

    @Test
    void findByParentIdReturnsChildrenWhenExist() {
        Branch parent = branchRepository.save(new Branch("Parent Branch", null));
        Branch child1 = branchRepository.save(new Branch("Child 1", parent));
        Branch child2 = branchRepository.save(new Branch("Child 2", parent));
        Branch unrelated = branchRepository.save(new Branch("Unrelated", null));

        List<Branch> children = branchRepository.findByParentId(parent.getId());
        assertNotNull(children);
        assertEquals(2, children.size());
        assertTrue(children.stream().anyMatch(b -> b.getName().equals("Child 1")));
        assertTrue(children.stream().anyMatch(b -> b.getName().equals("Child 2")));
        assertFalse(children.stream().anyMatch(b -> b.getName().equals("Unrelated")));
    }

    @Test
    void findByParentIdReturnsEmptyListWhenNoChildren() {
        Branch parent = branchRepository.save(new Branch("Parent No Children", null));
        List<Branch> children = branchRepository.findByParentId(parent.getId());
        assertNotNull(children);
        assertTrue(children.isEmpty());
    }

    @Test
    void findByParentIdReturnsEmptyListForNonExistentParent() {
        List<Branch> children = branchRepository.findByParentId(999L);
        assertNotNull(children);
        assertTrue(children.isEmpty());
    }
}