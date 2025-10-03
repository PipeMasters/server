package com.pipemasters.server.dto;

import com.pipemasters.server.entity.enums.AbsenceCause;

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

    private Set<Long> tagIds;

    private String comment;

    private Long id;

    private Boolean archived;

    private Boolean deleted;

    private AbsenceCause absenceCause;

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

    public Set<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<Long> tagIds) {
        this.tagIds = tagIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public AbsenceCause getAbsenceCause() {
        return absenceCause;
    }

    public void setAbsenceCause(AbsenceCause absenceCause) {
        this.absenceCause = absenceCause;
    }
}