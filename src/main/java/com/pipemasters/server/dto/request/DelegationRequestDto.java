package com.pipemasters.server.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class DelegationRequestDto {
    @NotNull(message = "DelegatorId cannot be empty")
    private Long delegatorId;
    @NotNull(message = "SubstituteId cannot be empty")
    private Long substituteId;
    @NotNull(message = "FromDate cannot be empty")
    private LocalDate fromDate;
    @NotNull(message = "ToDate cannot be empty")
    private LocalDate toDate;

    public DelegationRequestDto(Long delegatorId, Long substituteId, LocalDate fromDate, LocalDate toDate) {
        this.delegatorId = delegatorId;
        this.substituteId = substituteId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
    public DelegationRequestDto() {}

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
        return "DelegationDto{" +
                "delegatorId=" + delegatorId +
                ", substituteId=" + substituteId +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}
