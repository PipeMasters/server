package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Import TestEntityManager

import java.util.EnumSet;
import java.util.Set;
import java.util.Optional; // Import Optional

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void saveAndLoadUserWithRoles() {
        Branch branch = branchRepository.save(new Branch("Main", null));

        Set<Role> roles = EnumSet.of(Role.USER, Role.BRANCH_ADMIN);
        User user = new User("Ivan", "Petrov", "Sergeevich", roles, branch);
        userRepository.save(user);

        User found = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("Ivan", found.getName());
        assertEquals("Petrov", found.getSurname());
        assertEquals(branch.getId(), found.getBranch().getId());
        assertEquals(2, found.getRoles().size());
        assertTrue(found.getRoles().contains(Role.USER));
    }

    @Test
    void findByIdWithBranchFetchesBranchEagerly() {
        Branch parentBranch = new Branch("Headquarters", null);
        entityManager.persistAndFlush(parentBranch);

        Branch childBranch = new Branch("Regional Office", parentBranch);
        entityManager.persistAndFlush(childBranch);

        Set<Role> roles = EnumSet.of(Role.USER);
        User user = new User("John", "Doe", "Jr.", roles, childBranch);
        entityManager.persistAndFlush(user);

        entityManager.clear();

        Optional<User> foundUserOptional = userRepository.findByIdWithBranch(user.getId());

        assertTrue(foundUserOptional.isPresent(), "User should be found");
        User foundUser = foundUserOptional.get();

        assertEquals(user.getName(), foundUser.getName());
        assertEquals(user.getSurname(), foundUser.getSurname());
        assertEquals(user.getId(), foundUser.getId());

        assertNotNull(foundUser.getBranch(), "Branch should not be null");
        assertEquals(childBranch.getId(), foundUser.getBranch().getId(), "Branch ID should match");
        assertEquals(childBranch.getName(), foundUser.getBranch().getName(), "Branch name should match");

        if (foundUser.getBranch().getParent() != null) {
            assertEquals(parentBranch.getId(), foundUser.getBranch().getParent().getId());
        }
    }

    @Test
    void findByIdWithBranchReturnsEmptyOptionalForNonExistentUser() {
        Optional<User> foundUser = userRepository.findByIdWithBranch(999L);
        assertFalse(foundUser.isPresent(), "Should return empty optional for non-existent user");
    }
}