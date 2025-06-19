package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.TrainDTO;
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
    public TrainDTO save(TrainDTO trainDto) {
        Train train = modelMapper.map(trainDto, Train.class);
        return modelMapper.map(trainRepository.save(train), TrainDTO.class);
    }

    @Override
    public TrainDTO getById(Long id) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Train not found"));
        return modelMapper.map(train, TrainDTO.class);
    }

    @Override
    public List<TrainDTO> getAll() {
        return trainRepository.findAll().stream()
                .map(train -> modelMapper.map(train, TrainDTO.class))
                .toList();
    }

    @Override
    public TrainDTO update(Long id, TrainDTO trainDto) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Train not found"));
        train.setTrainNumber(trainDto.getTrainNumber());
        train.setRouteMessage(trainDto.getRouteMessage());
        train.setConsistCount(trainDto.getConsistCount());
        train.setChief(trainDto.getChief());
        return modelMapper.map(trainRepository.save(train), TrainDTO.class);
    }

    @Override
    public void delete(Long id) {
        trainRepository.deleteById(id);
    }
}