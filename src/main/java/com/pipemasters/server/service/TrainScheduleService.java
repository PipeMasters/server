package com.pipemasters.server.service;

import com.pipemasters.server.dto.ParsingStatsDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface TrainScheduleService {
    ParsingStatsDto parseExcelFile(MultipartFile file) throws IOException;
}