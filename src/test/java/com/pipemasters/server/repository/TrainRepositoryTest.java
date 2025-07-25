package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TrainRepositoryTest {

    @Autowired
    private TrainRepository trainRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    private Branch createBranch() {
        return branchRepository.save(new Branch("Test Branch " + UUID.randomUUID(), null));
    }

    private User createChief(String name) {
        Branch branch = createBranch();
        User chief = new User(name, "Last", "Middle", Set.of(Role.USER), branch);
        return userRepository.save(chief);
    }

    private Train createTrain() {
        User chief = createChief("Ivanov I.I.");
        Branch branch = chief.getBranch();
        return trainRepository.save(new Train(1001L, "Moscow — Saint Petersburg", 10, chief, branch));
    }

    private Train createTrain(long trainNumber, String routeMessage, int consistCount, String chiefName) {
        User chief = createChief(chiefName);
        Branch branch = chief.getBranch();
        return trainRepository.save(new Train(trainNumber, routeMessage, consistCount, chief, branch));
    }

    @Test
    void findAllReturnsEmptyListWhenNoTrainsExist() {
        List<Train> all = trainRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void saveAndFindTrainByIdReturnsCorrectTrain() {
        Train train = createTrain();
        Optional<Train> found = trainRepository.findById(train.getId());
        assertTrue(found.isPresent());
        assertEquals(train.getTrainNumber(), found.get().getTrainNumber());
        assertEquals(train.getRouteMessage(), found.get().getRouteMessage());
    }

    @Test
    void existsByIdReturnsTrueForExistingTrain() {
        Train train = createTrain();
        assertTrue(trainRepository.existsById(train.getId()));
    }

    @Test
    void existsByIdReturnsFalseForNonExistentTrain() {
        assertFalse(trainRepository.existsById(99999L));
    }

    @Test
    void deleteByIdRemovesTrain() {
        Train train = createTrain();
        trainRepository.deleteById(train.getId());
        assertFalse(trainRepository.existsById(train.getId()));
    }

    @Test
    void findByIdReturnsEmptyOptionalForNonExistentTrain() {
        assertTrue(trainRepository.findById(123456L).isEmpty());
    }

    @Test
    void saveMultipleTrainsWithSameChiefAllowed() {
        User chief = createChief("Petrov P.P.");
        Branch branch = chief.getBranch();
        Train train1 = trainRepository.save(new Train(2001L, "Kazan — Samara", 5, chief, branch));
        Train train2 = trainRepository.save(new Train(2002L, "Samara — Kazan", 6, chief, branch));
        assertNotNull(train1.getId());
        assertNotNull(train2.getId());
        assertNotEquals(train1.getId(), train2.getId());
    }

    @Test
    void saveTrainWithNullRouteMessageThrowsException() {
        User chief = createChief("Sidorov S.S.");
        Train train = new Train(3001L, null, 3, chief, chief.getBranch());
        assertThrows(Exception.class, () -> trainRepository.save(train));
    }

    @Test
    void saveTrainWithNullChiefThrowsException() {
        Train train = new Train(4001L, "Omsk — Tomsk", 2, null, null);
        assertThrows(Exception.class, () -> trainRepository.save(train));
    }

//    @Test
//    void findDistinctChiefsReturnsUniqueChiefsWhenTheyExist() {
//        createTrain(5001L, "Route 1", 1, "Иванов И.И.");
//        createTrain(5002L, "Route 2", 2, "Петров П.П.");
//        createTrain(5003L, "Route 3", 3, "Иванов И.И.");
//        createTrain(5004L, "Route 4", 4, "Сидоров С.С.");
//
//        List<String> uniqueChiefs = trainRepository.findDistinctChiefs();
//
//        assertNotNull(uniqueChiefs);
//        assertEquals(3, uniqueChiefs.size());
//        assertThat(uniqueChiefs).containsExactlyInAnyOrder("Иванов И.И.", "Петров П.П.", "Сидоров С.С.");
//    }

    @Test
    void findDistinctChiefsReturnsEmptyListWhenNoTrainsExist() {
        List<User> uniqueChiefs = trainRepository.findDistinctChiefs();

        assertNotNull(uniqueChiefs);
        assertTrue(uniqueChiefs.isEmpty());
    }

    @Test
    void findByBranchIdReturnsCorrectTrainsForBranch() {
        Branch branch1 = createBranch();
        User chief1 = new User("Chief A", "Last", "Middle", Set.of(Role.USER), branch1);
        userRepository.save(chief1);
        trainRepository.save(new Train(1L, "Route 1", 10, chief1, branch1));
        trainRepository.save(new Train(2L, "Route 2", 12, chief1, branch1));

        Branch branch2 = createBranch();
        User chief2 = new User("Chief B", "Last", "Middle", Set.of(Role.USER), branch2);
        userRepository.save(chief2);
        trainRepository.save(new Train(3L, "Route 3", 8, chief2, branch2));

        List<Train> trainsInBranch1 = trainRepository.findByBranchId(branch1.getId());
        List<Train> trainsInBranch2 = trainRepository.findByBranchId(branch2.getId());
        List<Train> trainsInNonExistentBranch = trainRepository.findByBranchId(999L);

        assertNotNull(trainsInBranch1);
        assertEquals(2, trainsInBranch1.size());
        assertTrue(trainsInBranch1.stream().allMatch(t -> t.getBranch().getId().equals(branch1.getId())));

        assertNotNull(trainsInBranch2);
        assertEquals(1, trainsInBranch2.size());
        assertTrue(trainsInBranch2.stream().allMatch(t -> t.getBranch().getId().equals(branch2.getId())));

        assertNotNull(trainsInNonExistentBranch);
        assertTrue(trainsInNonExistentBranch.isEmpty());
    }

    @Test
    void findDistinctChiefsReturnsUniqueChiefsWhenTheyExist() {
        Branch commonBranch = createBranch();

        User ivanov = userRepository.save(new User("Иванов И.И.", "И", "И", Set.of(Role.USER), commonBranch));
        User petrov = userRepository.save(new User("Петров П.П.", "П", "П", Set.of(Role.USER), commonBranch));
        User sidorov = userRepository.save(new User("Сидоров С.С.", "С", "С", Set.of(Role.USER), commonBranch));

        trainRepository.save(new Train(5001L, "Route 1", 1, ivanov, commonBranch));
        trainRepository.save(new Train(5002L, "Route 2", 2, petrov, commonBranch));
        trainRepository.save(new Train(5003L, "Route 3", 3, ivanov, commonBranch));
        trainRepository.save(new Train(5004L, "Route 4", 4, sidorov, commonBranch));

        List<User> uniqueChiefs = trainRepository.findDistinctChiefs();

        assertNotNull(uniqueChiefs);
        assertEquals(3, uniqueChiefs.size());
        assertThat(uniqueChiefs.stream().map(User::getName).collect(java.util.stream.Collectors.toSet()))
                .containsExactlyInAnyOrder("Иванов И.И.", "Петров П.П.", "Сидоров С.С.");
    }

    @Test
    void findDistinctChiefsByBranchIdReturnsCorrectChiefsForBranch() {
        Branch branchA = branchRepository.save(new Branch("Branch A", null));
        User chiefA1 = userRepository.save(new User("Chief A1", "L", "M", Set.of(Role.USER), branchA));
        User chiefA2 = userRepository.save(new User("Chief A2", "L", "M", Set.of(Role.USER), branchA));

        trainRepository.save(new Train(10L, "Route A1", 1, chiefA1, branchA));
        trainRepository.save(new Train(11L, "Route A2", 2, chiefA2, branchA));
        trainRepository.save(new Train(12L, "Route A3", 3, chiefA1, branchA));

        Branch branchB = branchRepository.save(new Branch("Branch B", null));
        User chiefB1 = userRepository.save(new User("Chief B1", "L", "M", Set.of(Role.USER), branchB));
        trainRepository.save(new Train(20L, "Route B1", 4, chiefB1, branchB));

        User chiefC = userRepository.save(new User("Chief C", "L", "M", Set.of(Role.USER), createBranch()));

        List<User> chiefsInBranchA = trainRepository.findDistinctChiefsByBranchId(branchA.getId());
        List<User> chiefsInBranchB = trainRepository.findDistinctChiefsByBranchId(branchB.getId());
        List<User> chiefsInNonExistentBranch = trainRepository.findDistinctChiefsByBranchId(999L);


        assertNotNull(chiefsInBranchA);
        assertEquals(2, chiefsInBranchA.size());
        assertThat(chiefsInBranchA.stream().map(User::getName).collect(java.util.stream.Collectors.toSet()))
                .containsExactlyInAnyOrder("Chief A1", "Chief A2");

        assertNotNull(chiefsInBranchB);
        assertEquals(1, chiefsInBranchB.size());
        assertThat(chiefsInBranchB.stream().map(User::getName).collect(java.util.stream.Collectors.toSet()))
                .containsExactlyInAnyOrder("Chief B1");

        assertNotNull(chiefsInNonExistentBranch);
        assertTrue(chiefsInNonExistentBranch.isEmpty());
    }

    @Test
    void findDistinctChiefsByBranchIdReturnsEmptyListWhenNoTrainsForBranch() {
        Branch branch = createBranch();
        User chief = createChief("Chief No Trains");

        List<User> chiefs = trainRepository.findDistinctChiefsByBranchId(branch.getId());
        assertNotNull(chiefs);
        assertTrue(chiefs.isEmpty());
    }
}