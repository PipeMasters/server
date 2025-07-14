package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager; // Import TestEntityManager

import java.util.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Branch createBranch(String namePrefix, Branch parent) {
        Branch branch = new Branch(namePrefix + " " + UUID.randomUUID(), parent);
        return branchRepository.save(branch);
    }

    private User createUser(String name, String surname, Branch branch) {
        User user = new User(name, surname, "Patronymic", EnumSet.of(Role.USER), branch);
        return userRepository.save(user);
    }

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

    @Test
    void findByBranchIdReturnsCorrectUsersForBranch() {
        Branch branch1 = createBranch("Branch A", null);
        User user1 = createUser("User1", "Surname1", branch1);
        User user2 = createUser("User2", "Surname2", branch1);

        Branch branch2 = createBranch("Branch B", null);
        User user3 = createUser("User3", "Surname3", branch2);


        List<User> usersInBranch1 = userRepository.findByBranchId(branch1.getId());
        List<User> usersInBranch2 = userRepository.findByBranchId(branch2.getId());
        List<User> usersInNonExistentBranch = userRepository.findByBranchId(999L);

        assertNotNull(usersInBranch1);
        assertEquals(2, usersInBranch1.size());
        assertThat(usersInBranch1).extracting(User::getId).containsExactlyInAnyOrder(user1.getId(), user2.getId());

        assertNotNull(usersInBranch2);
        assertEquals(1, usersInBranch2.size());
        assertThat(usersInBranch2).extracting(User::getId).containsExactlyInAnyOrder(user3.getId());

        assertNotNull(usersInNonExistentBranch);
        assertTrue(usersInNonExistentBranch.isEmpty());
    }

    @Test
    void findByBranchIdReturnsEmptyListWhenNoUsersForBranch() {
        Branch branch = createBranch("Empty Branch", null);
        List<User> users = userRepository.findByBranchId(branch.getId());
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}