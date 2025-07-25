package com.pipemasters.server.repository;

import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UploadBatchRepositoryTest {

    @Autowired
    private UploadBatchRepository uploadBatchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TrainRepository trainRepository;

    private static int branchCounter = 0;

    private Branch createBranch() {
        branchCounter++;
        return branchRepository.save(new Branch("Test Branch " + branchCounter, null));
    }

    private User createUser(Branch branch) {
        return userRepository.save(new User("Ivan", "Ivanov", "Ivanovich", Set.of(Role.USER), branch));
    }

    private User createChief(Branch branch) {
        return userRepository.save(new User("Petr", "Petrov", "Petrovich", Set.of(Role.USER), branch));
    }

    private Train createTrain(Branch branch) {
        User chief = createChief(branch);
        return trainRepository.save(new Train(123L, "Москва — Сочи", 1, chief, branch));
    }

    private UploadBatch createUploadBatch() {
        Branch branch = createBranch();
        User user = createUser(branch);
        Train train = createTrain(branch);
        LocalDate today = LocalDate.now();
        UploadBatch batch = new UploadBatch(
                UUID.randomUUID(),
                user,
                Instant.now(),
                today,
                train,
                "Комментарий",
                branch,
                false,
                null,
                false,
                new ArrayList<>()
        );
        batch.setTrainArrived(today.plusDays(1));
        return uploadBatchRepository.save(batch);
    }

    @Test
    void saveAndFindUploadBatch() {
        UploadBatch batch = createUploadBatch();
        Optional<UploadBatch> found = uploadBatchRepository.findById(batch.getId());
        assertTrue(found.isPresent());
        assertEquals(batch.getId(), found.get().getId());
    }

    @Test
    void saveRecordWithRelations() {
        Branch branch = branchRepository.save(new Branch("Central", null));
        User user = userRepository.save(new User("Maria", "Ivanova", "Petrovna",
                EnumSet.of(Role.USER), branch));
        User chief = userRepository.save(new User("Ivan", "Petrov", "Sergeevich",
                EnumSet.of(Role.USER), branch));
        Train train = trainRepository.save(new Train(100L, "A-B", 1, chief, branch));
        LocalDate today = LocalDate.now();

        UploadBatch uploadBatch = new UploadBatch(UUID.randomUUID(), user, Instant.now(), today,
                train, "test", branch, false, null, false, new ArrayList<>());
        uploadBatch.setTrainArrived(today.plusDays(1));
        uploadBatchRepository.save(uploadBatch);

        UploadBatch found = uploadBatchRepository.findById(uploadBatch.getId()).orElseThrow();
        assertEquals(user.getId(), found.getUploadedBy().getId());
        assertEquals(train.getId(), found.getTrain().getId());
        assertEquals(branch.getId(), found.getBranch().getId());
        assertFalse(found.isDeleted());
    }
}