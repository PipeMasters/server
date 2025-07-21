package com.pipemasters.server.service;

import org.springframework.scheduling.annotation.Scheduled;

public interface ImotioPollingSchedulerService {
    void addTaskToPoll(String imotioId, Long mediaFileId);

    void pollImotioStatuses();

    void processSingleImotioStatus(String imotioId, Long mediaFileId);
}
