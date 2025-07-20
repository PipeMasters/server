package com.pipemasters.server.dto.response;

import java.time.LocalDate;
import java.util.List;

public class UploadBatchSearchDto {
    private Long id;
    private LocalDate dateDeparted;
    private LocalDate dateArrived;
    private Long trainNumber;
    private String chiefName;
    private List<MediaFileFragmentsDto> files;

    public UploadBatchSearchDto() {
    }

    public UploadBatchSearchDto(Long id, LocalDate dateDeparted, LocalDate dateArrived,
                                Long trainNumber, String chiefName,
                                List<MediaFileFragmentsDto> files) {
        this.id = id;
        this.dateDeparted = dateDeparted;
        this.dateArrived = dateArrived;
        this.trainNumber = trainNumber;
        this.chiefName = chiefName;
        this.files = files;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateDeparted() {
        return dateDeparted;
    }

    public void setDateDeparted(LocalDate dateDeparted) {
        this.dateDeparted = dateDeparted;
    }

    public LocalDate getDateArrived() {
        return dateArrived;
    }

    public void setDateArrived(LocalDate dateArrived) {
        this.dateArrived = dateArrived;
    }

    public Long getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(Long trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getChiefName() {
        return chiefName;
    }

    public void setChiefName(String chiefName) {
        this.chiefName = chiefName;
    }

    public List<MediaFileFragmentsDto> getFiles() {
        return files;
    }

    public void setFiles(List<MediaFileFragmentsDto> files) {
        this.files = files;
    }
}