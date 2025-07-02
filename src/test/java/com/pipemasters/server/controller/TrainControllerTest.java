package com.pipemasters.server.controller;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.service.TrainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class TrainControllerTest {

    @Mock
    private TrainService trainService;

    @InjectMocks
    private TrainController trainController;

    @Test
    void create_ReturnsCreatedStatusAndDto() {
        TrainRequestDto input = new TrainRequestDto();
        TrainResponseDto saved = new TrainResponseDto();

        when(trainService.save(input)).thenReturn(saved);

        ResponseEntity<TrainResponseDto> response = trainController.create(input);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(saved, response.getBody());
    }

    @Test
    void get_ReturnsOkStatusAndDto() {
        Long id = 1L;
        TrainResponseDto dto = new TrainResponseDto();
        when(trainService.getById(id)).thenReturn(dto);

        ResponseEntity<TrainResponseDto> response = trainController.get(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void getAll_ReturnsOkStatusAndList() {
        List<TrainResponseDto> list = List.of(new TrainResponseDto(), new TrainResponseDto());
        when(trainService.getAll()).thenReturn(list);

        ResponseEntity<List<TrainResponseDto>> response = trainController.getAll();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(list, response.getBody());
    }

    @Test
    void update_ReturnsOkStatusAndUpdatedDto() {
        Long id = 1L;
        TrainRequestDto input = new TrainRequestDto();
        TrainResponseDto updated = new TrainResponseDto();

        when(trainService.update(id, input)).thenReturn(updated);

        ResponseEntity<TrainResponseDto> response = trainController.update(id, input);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
    }

    @Test
    void delete_ReturnsNoContentStatus() {
        Long id = 1L;

        ResponseEntity<Void> response = trainController.delete(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(trainService).delete(id);
    }

    @Test
    void getUniqueChiefs_ReturnsOkStatusAndListOfUniqueChiefs() {
        List<String> uniqueChiefs = Arrays.asList("Иванов", "Петров", "Сидоров");
        when(trainService.getUniqueChiefs()).thenReturn(uniqueChiefs);

        ResponseEntity<List<String>> response = trainController.getUniqueChiefs();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals(uniqueChiefs, response.getBody());
    }

    @Test
    void getUniqueChiefs_ReturnsEmptyListWhenNoChiefsExist() {
        when(trainService.getUniqueChiefs()).thenReturn(Collections.emptyList());

        ResponseEntity<List<String>> response = trainController.getUniqueChiefs();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }
}