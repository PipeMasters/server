package com.pipemasters.server.repository;

import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class VideoAbsenceRepositoryTest {

    @Autowired
    private VideoAbsenceRepository videoAbsenceRepository;

    @Autowired
    private UploadBatchRepository uploadBatchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private TrainRepository trainRepository;

    private Branch branch;
    private User user;
    private Train train;

    @BeforeEach
    void setup() {
        branch = branchRepository.save(new Branch("Test Branch", null));
        user = userRepository.save(new User("John", "Doe", "Middle", Set.of(Role.USER), branch));
        train = trainRepository.save(new Train(123L, "Moscow - Sochi", 1, "Ivanov I.I."));
    }

    private UploadBatch createUploadBatch() {
        return uploadBatchRepository.save(new UploadBatch(
                UUID.randomUUID(),
                user,
                Instant.now(),
                LocalDate.now(),
                train,
                "Test comment",
                Set.of("keyword1", "keyword2"),
                branch,
                false,
                null,
                false,
                new ArrayList<>()
        ));
    }

    @Test
    void saveVideoAbsenceWithValidDataIsPersisted() {
        UploadBatch batch = createUploadBatch();
        VideoAbsence absence = new VideoAbsence(batch, AbsenceCause.DEVICE_FAILURE, "Camera broken");
        VideoAbsence saved = videoAbsenceRepository.save(absence);
        assertNotNull(saved.getId());
        assertEquals(AbsenceCause.DEVICE_FAILURE, saved.getCause());
        assertEquals("Camera broken", saved.getComment());
        assertEquals(batch.getId(), saved.getUploadBatch().getId());
    }

    @Test
    void saveVideoAbsenceWithNullUploadBatchThrowsException() {
        VideoAbsence absence = new VideoAbsence(null, AbsenceCause.OTHER, "No batch");
        assertThrows(Exception.class, () -> videoAbsenceRepository.save(absence));
    }

    @Test
    void saveVideoAbsenceWithNullCauseThrowsException() {
        UploadBatch batch = createUploadBatch();
        VideoAbsence absence = new VideoAbsence(batch, null, "No cause");
        assertThrows(Exception.class, () -> videoAbsenceRepository.save(absence));
    }

    @Test
    void saveVideoAbsenceWithNullCommentIsAllowed() {
        UploadBatch batch = createUploadBatch();
        VideoAbsence absence = new VideoAbsence(batch, AbsenceCause.OTHER, null);
        VideoAbsence saved = videoAbsenceRepository.save(absence);
        assertNull(saved.getComment());
    }

    @Test
    void saveVideoAbsenceWithEmptyCommentIsAllowed() {
        UploadBatch batch = createUploadBatch();
        VideoAbsence absence = new VideoAbsence(batch, AbsenceCause.OTHER, "");
        VideoAbsence saved = videoAbsenceRepository.save(absence);
        assertEquals("", saved.getComment());
    }

    @Test
    void cannotSaveTwoVideoAbsencesForSameUploadBatch() {
        UploadBatch batch = createUploadBatch();
        videoAbsenceRepository.save(new VideoAbsence(batch, AbsenceCause.OTHER, "First"));
        VideoAbsence second = new VideoAbsence(batch, AbsenceCause.DEVICE_FAILURE, "Second");
        assertThrows(Exception.class, () -> videoAbsenceRepository.save(second));
    }

    @Test
    void findAllReturnsAllPersistedVideoAbsences() {
        UploadBatch batch1 = createUploadBatch();
        UploadBatch batch2 = createUploadBatch();
        videoAbsenceRepository.save(new VideoAbsence(batch1, AbsenceCause.OTHER, "Reason 1"));
        videoAbsenceRepository.save(new VideoAbsence(batch2, AbsenceCause.DEVICE_FAILURE, "Reason 2"));
        List<VideoAbsence> all = videoAbsenceRepository.findAll();
        assertEquals(2, all.size());
    }
}