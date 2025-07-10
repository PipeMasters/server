package com.pipemasters.server.controller;

import com.pipemasters.server.service.TrainScheduleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/train-schedules")
public class TrainScheduleController {

    private final TrainScheduleService trainScheduleService;

    public TrainScheduleController(TrainScheduleService trainScheduleService) {
        this.trainScheduleService = trainScheduleService;
    }

    // todo
}