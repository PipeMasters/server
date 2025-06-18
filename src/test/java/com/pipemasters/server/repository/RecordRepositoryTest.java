package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.Record;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RecordRepositoryTest {

    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TrainRepository trainRepository;

    @Test
    void saveRecordWithRelations() {
        Branch branch = branchRepository.save(new Branch("Central", null));
        User user = userRepository.save(new User("Maria", "Ivanova", "Petrovna",
                EnumSet.of(Role.USER), branch));
        Train train = trainRepository.save(new Train(100L, "A-B", 1, "Chief"));

        Record record = new Record(UUID.randomUUID(), user, Instant.now(), LocalDate.now(),
                train, "test", Set.of("key"), branch, null, false, new ArrayList<>());
        recordRepository.save(record);

        Record found = recordRepository.findById(record.getId()).orElseThrow();
        assertEquals(user.getId(), found.getUploadedBy().getId());
        assertEquals(train.getId(), found.getTrain().getId());
        assertEquals(branch.getId(), found.getBranch().getId());
        assertFalse(found.isDeleted());
    }
}