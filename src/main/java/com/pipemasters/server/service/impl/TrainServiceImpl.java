package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.TrainDto;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.exceptions.user.UserNotFoundException;
import com.pipemasters.server.repository.BranchRepository;
import com.pipemasters.server.repository.TrainRepository;
import com.pipemasters.server.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final ModelMapper modelMapper;

    public TrainServiceImpl(TrainRepository trainRepository, UserRepository userRepository, BranchRepository branchRepository, ModelMapper modelMapper) {
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public TrainDto save(TrainDto trainDto) {
        Train train = modelMapper.map(trainDto, Train.class);
        User chief = userRepository.findById(trainDto.getChiefId()).orElseThrow(() -> new UserNotFoundException("Chief user not found with ID: " + trainDto.getChiefId()));
        train.setChief(chief);
        if (trainDto.getBranchId() != null) {
            train.setBranch(branchRepository.findById(trainDto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + trainDto.getBranchId())));
        }
        return modelMapper.map(trainRepository.save(train), TrainDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainDto getById(Long id) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));
        return modelMapper.map(train, TrainDto.class);
    }

    @Override
    @Cacheable("trains")
    @Transactional(readOnly = true)
    public List<TrainDto> getAll() {
        return trainRepository.findAll().stream()
                .map(train -> modelMapper.map(train, TrainDto.class))
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public TrainDto update(Long id, TrainDto trainDto) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));
        User chief = userRepository.findById(trainDto.getChiefId()).orElseThrow(() -> new UserNotFoundException("Chief user not found with ID: " + trainDto.getChiefId()));
        train.setTrainNumber(trainDto.getTrainNumber());
        train.setRouteMessage(trainDto.getRouteMessage());
        train.setConsistCount(trainDto.getConsistCount());
        train.setChief(chief);
        if (trainDto.getBranchId() != null) {
            train.setBranch(branchRepository.findById(trainDto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + trainDto.getBranchId())));
        }
        return modelMapper.map(trainRepository.save(train), TrainDto.class);
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