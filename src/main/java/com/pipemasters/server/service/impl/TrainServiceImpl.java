package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.repository.TrainRepository;
import com.pipemasters.server.service.TrainService;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainServiceImpl implements TrainService {

    private final TrainRepository trainRepository;
    private final ModelMapper modelMapper;

    public TrainServiceImpl(TrainRepository trainRepository, ModelMapper modelMapper) {
        this.trainRepository = trainRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public TrainResponseDto save(TrainRequestDto trainRequestDto) {
        Train train = modelMapper.map(trainRequestDto, Train.class);
        return modelMapper.map(trainRepository.save(train), TrainResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainResponseDto getById(Long id) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));
        return modelMapper.map(train, TrainResponseDto.class);
    }

    @Override
    @Cacheable("trains")
    @Transactional(readOnly = true)
    public List<TrainResponseDto> getAll() {
        return trainRepository.findAll().stream()
                .map(train -> modelMapper.map(train, TrainResponseDto.class))
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public TrainResponseDto update(Long id, TrainRequestDto trainRequestDto) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));
        train.setTrainNumber(trainRequestDto.getTrainNumber());
        train.setRouteMessage(trainRequestDto.getRouteMessage());
        train.setConsistCount(trainRequestDto.getConsistCount());
        train.setChief(trainRequestDto.getChief());
        return modelMapper.map(trainRepository.save(train), TrainResponseDto.class);
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public void delete(Long id) {
        trainRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getUniqueChiefs() {
        return trainRepository.findDistinctChiefs();
    }
}