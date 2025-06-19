package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.repository.TrainRepository;
import com.pipemasters.server.service.TrainService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
    public TrainDto save(TrainDto trainDto) {
        Train train = modelMapper.map(trainDto, Train.class);
        return modelMapper.map(trainRepository.save(train), TrainDto.class);
    }

    @Override
    public TrainDto getById(Long id) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Train not found"));
        return modelMapper.map(train, TrainDto.class);
    }

    @Override
    public List<TrainDto> getAll() {
        return trainRepository.findAll().stream()
                .map(train -> modelMapper.map(train, TrainDto.class))
                .toList();
    }

    @Override
    public TrainDto update(Long id, TrainDto trainDto) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Train not found"));
        train.setTrainNumber(trainDto.getTrainNumber());
        train.setRouteMessage(trainDto.getRouteMessage());
        train.setConsistCount(trainDto.getConsistCount());
        train.setChief(trainDto.getChief());
        return modelMapper.map(trainRepository.save(train), TrainDto.class);
    }

    @Override
    public void delete(Long id) {
        trainRepository.deleteById(id);
    }
}