package com.pipemasters.server.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public class UploadBatchFilter {
    private LocalDate departureDateFrom;
    private LocalDate departureDateTo;

    private LocalDate arrivalDateFrom;
    private LocalDate arrivalDateTo;

    private LocalDate specificDate;

    private Long trainId;

    private Long chiefId;

    private Instant  createdFrom;

    private Instant createdTo;

    private Long uploadedById;

    private Long branchId;

    private String uploadedByName;

    private Set<String> tags;

//    private Set<String> keywords;

    public LocalDate getDepartureDateFrom() {
        return departureDateFrom;
    }

    public void setDepartureDateFrom(LocalDate departureDateFrom) {
        this.departureDateFrom = departureDateFrom;
    }

    public LocalDate getDepartureDateTo() {
        return departureDateTo;
    }

    public void setDepartureDateTo(LocalDate departureDateTo) {
        this.departureDateTo = departureDateTo;
    }

    public LocalDate getArrivalDateFrom() {
        return arrivalDateFrom;
    }

    public void setArrivalDateFrom(LocalDate arrivalDateFrom) {
        this.arrivalDateFrom = arrivalDateFrom;
    }

    public LocalDate getArrivalDateTo() {
        return arrivalDateTo;
    }

    public void setArrivalDateTo(LocalDate arrivalDateTo) {
        this.arrivalDateTo = arrivalDateTo;
    }

    public LocalDate getSpecificDate() {
        return specificDate;
    }

    public void setSpecificDate(LocalDate specificDate) {
        this.specificDate = specificDate;
    }

    public Long getTrainId() {
        return trainId;
    }

    public void setTrainId(Long trainId) {
        this.trainId = trainId;
    }

    public Long getChiefId() {
        return chiefId;
    }

    public void setChiefId(Long chiefId) {
        this.chiefId = chiefId;
    }

    public Instant getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(Instant createdFrom) {
        this.createdFrom = createdFrom;
    }

    public Instant getCreatedTo() {
        return createdTo;
    }

    public void setCreatedTo(Instant createdTo) {
        this.createdTo = createdTo;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getUploadedByName() {
        return uploadedByName;
    }

    public void setUploadedByName(String uploadedByName) {
        this.uploadedByName = uploadedByName;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

//    public Set<String> getKeywords() {
//        return keywords;
//    }

//    public void setKeywords(Set<String> keywords) {
//        this.keywords = keywords;
//    }
}