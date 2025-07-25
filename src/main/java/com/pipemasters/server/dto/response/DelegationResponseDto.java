package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelegationResponseDto {
    private Long delegatorId;
    private Long substituteId;
    private LocalDate fromDate;
    private LocalDate toDate;

    public DelegationResponseDto(Long delegatorId, Long substituteId, LocalDate fromDate, LocalDate toDate) {
        this.delegatorId = delegatorId;
        this.substituteId = substituteId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
    public DelegationResponseDto() {}

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
