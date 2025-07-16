package com.pipemasters.server.repository;

import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MediaFileRepositoryTest {

    @Autowired
    private MediaFileRepository mediaFileRepository;

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
        LocalDate departed = LocalDate.now();
        LocalDate arrived = departed.plusDays(1);
        UploadBatch batch = new UploadBatch(
                UUID.randomUUID(),
                user,
                Instant.now(),
                departed,
                train,
                "Комментарий",
                Set.of("ключевое", "слово"),
                branch,
                false,
                null,
                false,
                new ArrayList<>()
        );
        batch.setTrainArrived(arrived);
        return uploadBatchRepository.save(batch);
    }

    @Test
    void findAllReturnsEmptyListWhenNoMediaFilesExist() {
        List<MediaFile> all = mediaFileRepository.findAll();
        assertTrue(all.isEmpty());
    }

    @Test
    void findByIdReturnsEmptyOptionalForNonExistentMediaFile() {
        assertTrue(mediaFileRepository.findById(999L).isEmpty());
    }

    @Test
    void existsByIdReturnsFalseForNonExistentMediaFile() {
        assertFalse(mediaFileRepository.existsById(12345L));
    }

    @Test
    void saveMediaFileWithNullSourcePersistsSuccessfully() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = new MediaFile("file.mp4", FileType.VIDEO, batch);
        MediaFile saved = mediaFileRepository.save(file);
        assertNotNull(saved.getId());
        assertNull(saved.getSource());
        assertEquals("file.mp4", saved.getFilename());
    }

    @Test
    void saveAndFindMediaFileWithSourcePersistsHierarchy() {
        UploadBatch batch = createUploadBatch();
        MediaFile source = mediaFileRepository.save(new MediaFile("source.mp4", FileType.VIDEO, batch));
        MediaFile file = new MediaFile("child.mp3", FileType.AUDIO, batch);
        file.setSource(source);
        MediaFile saved = mediaFileRepository.save(file);
        Optional<MediaFile> found = mediaFileRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("child.mp3", found.get().getFilename());
        assertEquals(source.getId(), found.get().getSource().getId());
    }

    @Test
    void findAllReturnsAllMediaFiles() {
        UploadBatch batch = createUploadBatch();
        mediaFileRepository.save(new MediaFile("a.mp4", FileType.VIDEO, batch));
        mediaFileRepository.save(new MediaFile("b.mp3", FileType.AUDIO, batch));
        List<MediaFile> all = mediaFileRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void saveMediaFileWithNullFilenameThrowsException() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = new MediaFile(null, FileType.VIDEO, batch);
        assertThrows(Exception.class, () -> mediaFileRepository.save(file));
    }

    @Test
    void saveMediaFileWithNullUploadBatchThrowsException() {
        MediaFile file = new MediaFile("file.mp4", FileType.VIDEO, null);
        assertThrows(Exception.class, () -> mediaFileRepository.save(file));
    }

    @Test
    void saveMultipleMediaFilesWithSameFilenameAllowed() {
        UploadBatch batch = createUploadBatch();
        mediaFileRepository.save(new MediaFile("duplicate.mp4", FileType.VIDEO, batch));
        MediaFile second = new MediaFile("duplicate.mp4", FileType.VIDEO, batch);
        MediaFile saved = mediaFileRepository.save(second);
        assertNotNull(saved.getId());
        assertEquals("duplicate.mp4", saved.getFilename());
    }

    @Test
    void deleteMediaFileRemovesItFromRepository() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = mediaFileRepository.save(new MediaFile("toDelete.mp4", FileType.VIDEO, batch));
        mediaFileRepository.deleteById(file.getId());
        assertFalse(mediaFileRepository.existsById(file.getId()));
    }

    @Test
    void saveMediaFileWithNullFileTypeThrowsException() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = new MediaFile("file.mp4", null, batch);
        assertThrows(Exception.class, () -> mediaFileRepository.save(file));
    }

    @Test
    void saveMediaFileWithCustomUploadedAtPersistsCorrectly() {
        UploadBatch batch = createUploadBatch();
        Instant customInstant = Instant.parse("2023-01-01T10:00:00Z");
        MediaFile file = new MediaFile("custom.mp4", FileType.VIDEO, customInstant, null, batch);
        MediaFile saved = mediaFileRepository.save(file);
        assertEquals(customInstant, saved.getUploadedAt());
    }

    @Test
    void saveMediaFileWithItselfAsSourcePersistsSuccessfully() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = new MediaFile("selfsource.mp4", FileType.VIDEO, batch);
        file.setSource(file);
        MediaFile saved = mediaFileRepository.save(file);
        assertEquals(saved.getId(), saved.getSource().getId());
    }

    @Test
    void deleteByIdDoesNothingWhenMediaFileDoesNotExist() {
        mediaFileRepository.deleteById(99999L);
        assertTrue(mediaFileRepository.findAll().isEmpty());
    }

    @Test
    void findByIdAfterDeleteReturnsEmptyOptional() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = mediaFileRepository.save(new MediaFile("deleteCheck.mp4", FileType.VIDEO, batch));
        mediaFileRepository.deleteById(file.getId());
        assertTrue(mediaFileRepository.findById(file.getId()).isEmpty());
    }

    @Test
    void saveMediaFileWithLongFilenamePersistsSuccessfully() {
        UploadBatch batch = createUploadBatch();
        String longFilename = "a".repeat(255) + ".mp4";
        MediaFile file = new MediaFile(longFilename, FileType.VIDEO, batch);
        MediaFile saved = mediaFileRepository.save(file);
        assertEquals(longFilename, saved.getFilename());
    }

    @Test
    void saveMediaFileWithSourceFromAnotherBatchPersistsSuccessfully() {
        UploadBatch batch1 = createUploadBatch();
        UploadBatch batch2 = createUploadBatch();
        MediaFile source = mediaFileRepository.save(new MediaFile("source.mp4", FileType.VIDEO, batch1));
        MediaFile file = new MediaFile("child.mp4", FileType.VIDEO, batch2);
        file.setSource(source);
        MediaFile saved = mediaFileRepository.save(file);
        assertEquals(source.getId(), saved.getSource().getId());
        assertEquals(batch2.getId(), saved.getUploadBatch().getId());
    }

    @Test
    void saveMediaFileWithNullSourceAndNullUploadedAtUsesDefaultInstant() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = new MediaFile("defaultTime.mp4", FileType.VIDEO, batch);
        MediaFile saved = mediaFileRepository.save(file);
        assertNotNull(saved.getUploadedAt());
    }
    @Test
    void findByFilenameAndUploadBatchDirectoryReturnsMediaFileWhenExists() {
        UploadBatch batch = createUploadBatch();
        MediaFile file = new MediaFile("unique.mp4", FileType.VIDEO, batch);
        mediaFileRepository.save(file);
        Optional<MediaFile> found = mediaFileRepository.findByFilenameAndUploadBatchDirectory("unique.mp4", batch.getDirectory());
        assertTrue(found.isPresent());
        assertEquals(file.getId(), found.get().getId());
    }

    @Test
    void findByFilenameAndUploadBatchDirectoryReturnsEmptyOptionalWhenNotExists() {
        UploadBatch batch = createUploadBatch();
        Optional<MediaFile> found = mediaFileRepository.findByFilenameAndUploadBatchDirectory("nonexistent.mp4", batch.getDirectory());
        assertTrue(found.isEmpty());
    }

    @Test
    void findByFilenameAndUploadBatchDirectoryReturnsEmptyOptionalForDifferentBatch() {
        UploadBatch batch1 = createUploadBatch();
        UploadBatch batch2 = createUploadBatch();
        MediaFile file = new MediaFile("shared.mp4", FileType.VIDEO, batch1);
        mediaFileRepository.save(file);
        Optional<MediaFile> found = mediaFileRepository.findByFilenameAndUploadBatchDirectory("shared.mp4", batch2.getDirectory());
        assertTrue(found.isEmpty());
    }

    @Test
    void findByUploadBatchIdReturnsCorrectMediaFiles() {
        UploadBatch batch1 = createUploadBatch();
        UploadBatch batch2 = createUploadBatch();

        MediaFile file1Batch1 = mediaFileRepository.save(new MediaFile("file1_b1.mp4", FileType.VIDEO, batch1));
        MediaFile file2Batch1 = mediaFileRepository.save(new MediaFile("file2_b1.mp3", FileType.AUDIO, batch1));
        MediaFile file1Batch2 = mediaFileRepository.save(new MediaFile("file1_b2.jpg", FileType.IMAGE, batch2));

        List<MediaFile> foundFilesForBatch1 = mediaFileRepository.findByUploadBatchId(batch1.getId());
        assertNotNull(foundFilesForBatch1);
        assertEquals(2, foundFilesForBatch1.size());
        assertTrue(foundFilesForBatch1.contains(file1Batch1));
        assertTrue(foundFilesForBatch1.contains(file2Batch1));
        assertFalse(foundFilesForBatch1.contains(file1Batch2));

        List<MediaFile> foundFilesForBatch2 = mediaFileRepository.findByUploadBatchId(batch2.getId());
        assertNotNull(foundFilesForBatch2);
        assertEquals(1, foundFilesForBatch2.size());
        assertTrue(foundFilesForBatch2.contains(file1Batch2));
        assertFalse(foundFilesForBatch2.contains(file1Batch1));
    }

    @Test
    void findByUploadBatchIdReturnsEmptyListForBatchWithNoFiles() {
        UploadBatch batchWithNoFiles = createUploadBatch();

        List<MediaFile> foundFiles = mediaFileRepository.findByUploadBatchId(batchWithNoFiles.getId());
        assertNotNull(foundFiles);
        assertTrue(foundFiles.isEmpty());
    }

    @Test
    void findByUploadBatchIdReturnsEmptyListForNonExistentBatchId() {
        Long nonExistentBatchId = 999999L;

        List<MediaFile> foundFiles = mediaFileRepository.findByUploadBatchId(nonExistentBatchId);
        assertNotNull(foundFiles);
        assertTrue(foundFiles.isEmpty());
    }
}