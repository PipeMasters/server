package com.pipemasters.server.repository;

import com.pipemasters.server.entity.TrainSchedule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TrainScheduleRepositoryTest {

    @Autowired
    private TrainScheduleRepository trainScheduleRepository;

    @Autowired
    private TestEntityManager entityManager;

    private TrainSchedule createAndPersistTrain(String trainNumber, String category) {
        TrainSchedule train = new TrainSchedule();
        train.setTrainNumber(trainNumber);
        train.setCategory(category);
        train.setDepartureStation("Station A");
        train.setArrivalStation("Station B");
        train.setTravelTime(Duration.ofHours(5));
        train.setDepartureTime(LocalTime.of(9, 0));
        train.setArrivalTime(LocalTime.of(14, 0));
        train.setFirm(true);
        train.setPeriodicity("Daily");
        train.setSeasonality("All Year");
        return entityManager.persistAndFlush(train);
    }

    @Test
    @DisplayName("Should find no train schedules when the repository is empty")
    void findAllReturnsEmptyListWhenNoTrainsExist() {
        List<TrainSchedule> allTrains = trainScheduleRepository.findAll();
        assertTrue(allTrains.isEmpty(), "The list of train schedules should be empty.");
    }

    @Test
    @DisplayName("Should return an empty Optional when searching for a non-existent train number")
    void findByTrainNumberReturnsEmptyOptionalForNonExistentTrain() {
        Optional<TrainSchedule> foundTrain = trainScheduleRepository.findByTrainNumber("NONEXISTENT123");
        assertTrue(foundTrain.isEmpty(), "Optional should be empty for a non-existent train number.");
    }

    @Test
    @DisplayName("Should return an empty Optional when searching for a non-existent ID")
    void findByIdReturnsEmptyOptionalForNonExistentTrain() {
        Optional<TrainSchedule> foundTrain = trainScheduleRepository.findById(999L);
        assertTrue(foundTrain.isEmpty(), "Optional should be empty for a non-existent ID.");
    }

    @Test
    @DisplayName("Should return false when checking existence by ID for a non-existent train")
    void existsByIdReturnsFalseForNonExistentTrain() {
        boolean exists = trainScheduleRepository.existsById(12345L);
        assertFalse(exists, "existsById should return false for a non-existent ID.");
    }

    @Test
    @DisplayName("Should find a train schedule by its train number successfully")
    void findByTrainNumber_shouldReturnCorrectTrain() {
        TrainSchedule savedTrain = createAndPersistTrain("001A", "Express");

        Optional<TrainSchedule> foundTrain = trainScheduleRepository.findByTrainNumber("001A");

        assertTrue(foundTrain.isPresent(), "Train should be found by its number.");
        assertEquals(savedTrain.getId(), foundTrain.get().getId());
        assertEquals("001A", foundTrain.get().getTrainNumber());
    }

    @Test
    @DisplayName("Should find a train schedule by its train number case-sensitively (if DB is case-sensitive)")
    void findByTrainNumber_shouldBeCaseSensitive() {
        createAndPersistTrain("002B", "Local");
        createAndPersistTrain("002b", "Local");

        Optional<TrainSchedule> foundTrain = trainScheduleRepository.findByTrainNumber("002B");

        assertTrue(foundTrain.isPresent(), "Train '002B' should be found.");
        assertEquals("002B", foundTrain.get().getTrainNumber());

        Optional<TrainSchedule> foundTrainLower = trainScheduleRepository.findByTrainNumber("002b");
        assertTrue(foundTrainLower.isPresent(), "Train '002b' should also be found.");
        assertEquals("002b", foundTrainLower.get().getTrainNumber());
    }

    @Test
    @DisplayName("Should find multiple train schedules by a collection of train numbers")
    void findByTrainNumberIn_shouldReturnMultipleTrains() {
        TrainSchedule train1 = createAndPersistTrain("T001", "Fast");
        TrainSchedule train2 = createAndPersistTrain("T002", "Slow");
        createAndPersistTrain("T003", "Medium");

        Collection<String> searchNumbers = Arrays.asList("T001", "T002");

        List<TrainSchedule> foundTrains = trainScheduleRepository.findByTrainNumberIn(searchNumbers);

        assertNotNull(foundTrains, "Found trains list should not be null.");
        assertEquals(2, foundTrains.size(), "Should find exactly 2 trains.");
        assertTrue(foundTrains.stream().anyMatch(t -> t.getTrainNumber().equals("T001")));
        assertTrue(foundTrains.stream().anyMatch(t -> t.getTrainNumber().equals("T002")));
    }

    @Test
    @DisplayName("Should return an empty list when no matching train numbers are found in the collection")
    void findByTrainNumberIn_shouldReturnEmptyListForNoMatches() {
        createAndPersistTrain("T004", "Express");

        Collection<String> searchNumbers = Arrays.asList("NONEXISTENT4", "NONEXISTENT5");

        List<TrainSchedule> foundTrains = trainScheduleRepository.findByTrainNumberIn(searchNumbers);

        assertNotNull(foundTrains, "Found trains list should not be null.");
        assertTrue(foundTrains.isEmpty(), "Should return an empty list if no matches are found.");
    }

    @Test
    @DisplayName("Should return an empty list when the input collection of train numbers is empty")
    void findByTrainNumberIn_shouldReturnEmptyListForEmptyInputCollection() {
        createAndPersistTrain("T005", "Freight");

        Collection<String> emptySearchNumbers = Collections.emptyList();

        List<TrainSchedule> foundTrains = trainScheduleRepository.findByTrainNumberIn(emptySearchNumbers);

        assertNotNull(foundTrains, "Found trains list should not be null.");
        assertTrue(foundTrains.isEmpty(), "Should return an empty list for an empty input collection.");
    }

    @Test
    @DisplayName("Should find trains even if some numbers in the collection do not exist")
    void findByTrainNumberIn_shouldFindPartialMatches() {
        TrainSchedule train6 = createAndPersistTrain("T006", "Intercity");
        createAndPersistTrain("T007", "Suburban");

        Collection<String> searchNumbers = Arrays.asList("T006", "NONEXISTENT7", "ANOTHER_MISSING");

        List<TrainSchedule> foundTrains = trainScheduleRepository.findByTrainNumberIn(searchNumbers);

        assertNotNull(foundTrains, "Found trains list should not be null.");
        assertEquals(1, foundTrains.size(), "Should find only the existing train.");
        assertEquals(train6.getTrainNumber(), foundTrains.get(0).getTrainNumber());
    }

    @Test
    @DisplayName("Should save a new TrainSchedule entity successfully")
    void save_shouldPersistNewTrain() {
        TrainSchedule newTrain = new TrainSchedule();
        newTrain.setTrainNumber("NEW888");
        newTrain.setCategory("Test Category");
        newTrain.setDepartureStation("Start");
        newTrain.setArrivalStation("End");
        newTrain.setTravelTime(Duration.ofHours(3));
        newTrain.setDepartureTime(LocalTime.of(10, 0));
        newTrain.setArrivalTime(LocalTime.of(13, 0));
        newTrain.setFirm(false);
        newTrain.setPeriodicity("Weekends");
        newTrain.setSeasonality("Winter");

        TrainSchedule savedTrain = trainScheduleRepository.save(newTrain);
        entityManager.flush();

        assertNotNull(savedTrain.getId(), "Saved train should have an ID.");
        assertEquals("NEW888", savedTrain.getTrainNumber());

        Optional<TrainSchedule> found = trainScheduleRepository.findById(savedTrain.getId());
        assertTrue(found.isPresent());
        assertEquals("NEW888", found.get().getTrainNumber());
        assertEquals("Test Category", found.get().getCategory());
    }

    @Test
    @DisplayName("Should update an existing TrainSchedule entity successfully")
    void save_shouldUpdateExistingTrain() {
        TrainSchedule existingTrain = createAndPersistTrain("UPDATE999", "Old Category");
        entityManager.clear();

        Optional<TrainSchedule> retrievedTrain = trainScheduleRepository.findByTrainNumber("UPDATE999");
        assertTrue(retrievedTrain.isPresent());
        TrainSchedule trainToUpdate = retrievedTrain.get();
        trainToUpdate.setCategory("New Category");
        trainToUpdate.setPeriodicity("Daily (Updated)");

        TrainSchedule updatedTrain = trainScheduleRepository.save(trainToUpdate);
        entityManager.flush();
        entityManager.clear();

        Optional<TrainSchedule> foundUpdated = trainScheduleRepository.findById(existingTrain.getId());
        assertTrue(foundUpdated.isPresent());
        assertEquals("New Category", foundUpdated.get().getCategory());
        assertEquals("Daily (Updated)", foundUpdated.get().getPeriodicity());
    }
}