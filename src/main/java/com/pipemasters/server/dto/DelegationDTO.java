package com.pipemasters.server.dto;

import java.time.LocalDate;

public class DelegationDTO {
    private Long delegatorId;
    private Long substituteId;
    private LocalDate fromDate;
    private LocalDate toDate;

    public DelegationDTO(Long delegatorId, Long substituteId, LocalDate fromDate, LocalDate toDate) {
        this.delegatorId = delegatorId;
        this.substituteId = substituteId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
    public DelegationDTO() {}

    public Long getDelegatorId() {
        return delegatorId;
    }

    public void setDelegatorId(Long delegatorId) {
        this.delegatorId = delegatorId;
    }

    public Long getSubstituteId() {
        return substituteId;
    }

    public void setSubstituteId(Long substituteId) {
        this.substituteId = substituteId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return "DelegationDTO{" +
                "delegatorId=" + delegatorId +
                ", substituteId=" + substituteId +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
