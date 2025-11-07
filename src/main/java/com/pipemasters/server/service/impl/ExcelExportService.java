package com.pipemasters.server.service.impl;

import com.pipemasters.server.entity.Branch;
import com.pipemasters.server.entity.TrainSchedule;
import com.pipemasters.server.entity.User;
import com.pipemasters.server.entity.enums.Role;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExcelExportService {

    public ByteArrayOutputStream exportTrainScheduleToExcel(List<TrainSchedule> schedules) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Расписание поездов");

            String[] headers = {
                    "Номер поезда", "Категория", "Станция отправления", "Станция прибытия",
                    "Пользовательское имя", "Информация о ЖД", "Время в пути (ЧЧ:ММ)", "Время отправления",
                    "Время прибытия", "Фирменный", "Периодичность", "Сезонность", "Парный поезд"
            };

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (TrainSchedule schedule : schedules) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(schedule.getTrainNumber());
                row.createCell(1).setCellValue(schedule.getCategory());
                row.createCell(2).setCellValue(schedule.getDepartureStation());
                row.createCell(3).setCellValue(schedule.getArrivalStation());
                row.createCell(4).setCellValue(schedule.getCustomName());
                row.createCell(5).setCellValue(schedule.getRailwayInfo());
                row.createCell(6).setCellValue(formatDuration(schedule.getTravelTime()));
                row.createCell(7).setCellValue(formatLocalTime(schedule.getDepartureTime()));
                row.createCell(8).setCellValue(formatLocalTime(schedule.getArrivalTime()));
                row.createCell(9).setCellValue(schedule.isFirm() ? "Да" : "Нет");
                row.createCell(10).setCellValue(schedule.getPeriodicity());
                row.createCell(11).setCellValue(schedule.getSeasonality());

                String pairTrainNumber = Optional.ofNullable(schedule.getPairTrain())
                        .map(TrainSchedule::getTrainNumber)
                        .orElse("");
                row.createCell(12).setCellValue(pairTrainNumber);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        }
    }

    public ByteArrayOutputStream exportBranchesToExcel(List<Branch> branches) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Филиалы");

            String[] headers = {"Название", "Родительский филиал"};

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (Branch branch : branches) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(branch.getName());

                String parentName = Optional.ofNullable(branch.getParent())
                        .map(Branch::getName)
                        .orElse("");
                row.createCell(1).setCellValue(parentName);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        }
    }

    public ByteArrayOutputStream exportUsersToExcel(List<User> users) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Пользователи");

            String[] headers = {"Фамилия", "Имя", "Отчество", "Филиал", "Роли"};

            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getSurname());
                row.createCell(1).setCellValue(user.getName());
                row.createCell(2).setCellValue(user.getPatronymic());

                String branchName = Optional.ofNullable(user.getBranch())
                        .map(Branch::getName)
                        .orElse("");
                row.createCell(3).setCellValue(branchName);

                String roles = Optional.ofNullable(user.getRoles())
                        .orElse(Collections.emptySet())
                        .stream()
                        .map(Role::name)
                        .collect(Collectors.joining(", "));
                row.createCell(4).setCellValue(roles);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out;
        }
    }

    private String formatDuration(Duration duration) {
        if (duration == null) {
            return "";
        }
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        return String.format("%02d:%02d", hours, minutes);
    }

    private String formatLocalTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}