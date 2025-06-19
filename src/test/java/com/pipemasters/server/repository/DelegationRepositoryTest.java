package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Delegation;
import com.pipemasters.server.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class DelegationRepositoryTest {

    @Autowired
    private DelegationRepository delegationRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findAllReturnsEmptyListWhenNoDelegationsExist() {
        List<Delegation> all = delegationRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void findByIdReturnsEmptyOptionalForNonExistentDelegation() {
        assertTrue(delegationRepository.findById(999L).isEmpty());
    }

    @Test
    void existsByIdReturnsFalseForNonExistentDelegation() {
        assertFalse(delegationRepository.existsById(12345L));
    }

    @Test
    void saveDelegationWithNullDatesPersistsSuccessfully() {
        User delegator = userRepository.save(new User("A", "B", "C", Set.of(), null));
        User substitute = userRepository.save(new User("X", "Y", "Z", Set.of(), null));
        Delegation delegation = new Delegation(delegator, substitute, null, null);
        Delegation saved = delegationRepository.save(delegation);
        assertNotNull(saved.getId());
        assertNull(saved.getFromDate());
        assertNull(saved.getToDate());
    }

    @Test
    void saveDelegationWithSameDelegatorAndSubstitutePersistsSuccessfully() {
        User user = userRepository.save(new User("A", "B", "C", Set.of(), null));
        Delegation delegation = new Delegation(user, user, LocalDate.now(), LocalDate.now().plusDays(1));
        Delegation saved = delegationRepository.save(delegation);
        assertNotNull(saved.getId());
        assertEquals(user.getId(), saved.getDelegator().getId());
        assertEquals(user.getId(), saved.getSubstitute().getId());
    }

    @Test
    void saveAndFindDelegationPersistsAllFields() {
        User delegator = userRepository.save(new User("D", "E", "F", Set.of(), null));
        User substitute = userRepository.save(new User("G", "H", "I", Set.of(), null));
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(5);
        Delegation delegation = new Delegation(delegator, substitute, from, to);
        Delegation saved = delegationRepository.save(delegation);
        Delegation found = delegationRepository.findById(saved.getId()).orElseThrow();
        assertEquals(delegator.getId(), found.getDelegator().getId());
        assertEquals(substitute.getId(), found.getSubstitute().getId());
        assertEquals(from, found.getFromDate());
        assertEquals(to, found.getToDate());
    }

    @Test
    void findAllReturnsAllDelegations() {
        User u1 = userRepository.save(new User("A", "B", "C", Set.of(), null));
        User u2 = userRepository.save(new User("X", "Y", "Z", Set.of(), null));
        delegationRepository.save(new Delegation(u1, u2, LocalDate.now(), LocalDate.now().plusDays(1)));
        delegationRepository.save(new Delegation(u2, u1, LocalDate.now(), LocalDate.now().plusDays(2)));
        List<Delegation> all = delegationRepository.findAll();
        assertEquals(2, all.size());
    }
}