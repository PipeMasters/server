package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

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
}