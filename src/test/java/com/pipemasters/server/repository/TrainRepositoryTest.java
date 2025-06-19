package com.pipemasters.server.repository;

import com.pipemasters.server.entity.Train;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TrainRepositoryTest {

    @Autowired
    private TrainRepository trainRepository;

    private Train createTrain() {
        return trainRepository.save(new Train(1001L, "Moscow — Saint Petersburg", 10, "Ivanov I.I."));
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
        Train train1 = trainRepository.save(new Train(2001L, "Kazan — Samara", 5, "Petrov P.P."));
        Train train2 = trainRepository.save(new Train(2002L, "Samara — Kazan", 6, "Petrov P.P."));
        assertNotNull(train1.getId());
        assertNotNull(train2.getId());
        assertNotEquals(train1.getId(), train2.getId());
    }

    @Test
    void saveTrainWithNullRouteMessageThrowsException() {
        Train train = new Train(3001L, null, 3, "Sidorov S.S.");
        assertThrows(Exception.class, () -> trainRepository.save(train));
    }

    @Test
    void saveTrainWithNullChiefThrowsException() {
        Train train = new Train(4001L, "Omsk — Tomsk", 2, null);
        assertThrows(Exception.class, () -> trainRepository.save(train));
    }
}