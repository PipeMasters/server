package com.pipemasters.server.dto;

import java.time.LocalDate;

public class UploadBatchDtoSmallResponse {
    private Long id;
    private LocalDate dateDeparted;
    private LocalDate dateArrived;
    private Long trainNumber;
    private String chiefName;
    private String branchName;

    public UploadBatchDtoSmallResponse() {
    }

    public UploadBatchDtoSmallResponse(Long id, LocalDate dateDeparted, LocalDate dateArrived, Long trainNumber, String chiefName, String branchName) {
        this.id = id;
        this.dateDeparted = dateDeparted;
        this.dateArrived = dateArrived;
        this.trainNumber = trainNumber;
        this.chiefName = chiefName;
        this.branchName = branchName;
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

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    @Override
    public String toString() {
        return "UploadBatchDto{" +
                "id=" + id +
                ", dateFrom=" + dateDeparted +
                ", dateTo=" + dateArrived +
                ", trainNumber=" + trainNumber +
                ", chiefName='" + chiefName + '\'' +
                '}';
    }
}
