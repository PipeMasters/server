package com.pipemasters.server.service.impl;

import com.pipemasters.server.dto.request.TrainRequestDto;
import com.pipemasters.server.dto.response.TrainResponseDto;
import com.pipemasters.server.dto.response.UserResponseDto;
import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.Train;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.exceptions.branch.BranchNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNotFoundException;
import com.pipemasters.server.exceptions.train.TrainNumberExistsException;
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
    public TrainResponseDto save(TrainRequestDto trainDto) {
        if (trainRepository.existsByTrainNumber(trainDto.getTrainNumber())) {
            throw new TrainNumberExistsException("Train with number " + trainDto.getTrainNumber() + " already exists.");
        }
        Train train = modelMapper.map(trainDto, Train.class);
        User chief = userRepository.findById(trainDto.getChiefId()).orElseThrow(() -> new UserNotFoundException("Chief user not found with ID: " + trainDto.getChiefId()));
        train.setChief(chief);
        if (trainDto.getBranchId() != null) {
            train.setBranch(branchRepository.findById(trainDto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + trainDto.getBranchId())));
        }
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
    public TrainResponseDto update(Long id, TrainRequestDto trainDto) {
        Train train = trainRepository.findById(id).orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + id));
        if (!train.getTrainNumber().equals(trainDto.getTrainNumber())) {
            if (trainRepository.existsByTrainNumber(trainDto.getTrainNumber())) {
                throw new TrainNumberExistsException("Train with number " + trainDto.getTrainNumber() + " already exists.");
            }
        }
        User chief = userRepository.findById(trainDto.getChiefId()).orElseThrow(() -> new UserNotFoundException("Chief user not found with ID: " + trainDto.getChiefId()));
        train.setTrainNumber(trainDto.getTrainNumber());
        train.setRouteMessage(trainDto.getRouteMessage());
        train.setConsistCount(trainDto.getConsistCount());
        train.setChief(chief);
        if (trainDto.getBranchId() != null) {
            train.setBranch(branchRepository.findById(trainDto.getBranchId())
                    .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + trainDto.getBranchId())));
        }
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
    public List<UserResponseDto> getChiefs() {
        return trainRepository.findDistinctChiefs().stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public TrainResponseDto assignTrainToBranch(Long trainId, Long branchId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + trainId));

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with ID: " + branchId));

        train.setBranch(branch);
        return modelMapper.map(trainRepository.save(train), TrainResponseDto.class);
    }

    @Override
    @CacheEvict(cacheNames = "trains", allEntries = true)
    @Transactional
    public TrainResponseDto updateTrainChief(Long trainId, Long newChiefId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new TrainNotFoundException("Train not found with ID: " + trainId));

        User newChief = userRepository.findById(newChiefId)
                .orElseThrow(() -> new UserNotFoundException("Chief user not found with ID: " + newChiefId));

        train.setChief(newChief);
        return modelMapper.map(trainRepository.save(train), TrainResponseDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainResponseDto> getTrainsByBranchId(Long branchId) {
         if (!branchRepository.existsById(branchId)) {
             throw new BranchNotFoundException("Branch not found with id: " + branchId);
         }
        return trainRepository.findByBranchId(branchId).stream()
                .map(train -> modelMapper.map(train, TrainResponseDto.class))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getChiefsByBranchId(Long branchId) {
         if (!branchRepository.existsById(branchId)) {
             throw new BranchNotFoundException("Branch not found with id: " + branchId);
         }
        return trainRepository.findDistinctChiefsByBranchId(branchId).stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .toList();
    }
}