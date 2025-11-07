package com.pipemasters.server.service;

import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.entity.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public interface ExcelExportService {
    ByteArrayOutputStream exportTrainScheduleToExcel(List<TrainSchedule> schedules) throws IOException;
    ByteArrayOutputStream exportBranchesToExcel(List<Branch> branches) throws IOException;
    ByteArrayOutputStream exportUsersToExcel(List<User> users) throws IOException;
}
