package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.entity.User;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelegationDto extends BaseDto{
    @NotNull(message = "Delegator cannot be empty")
    private UserDto delegator;
    @NotNull(message = "Substitute cannot be empty")
    private UserDto substitute;
    @NotNull(message = "FromDate cannot be empty")
    private LocalDate fromDate;
    @NotNull(message = "ToDate cannot be empty")
    private LocalDate toDate;

    public DelegationDto() {
    }

    public DelegationDto( UserDto delegator, UserDto substitute, LocalDate fromDate, LocalDate toDate) {
        this.delegator = delegator;
        this.substitute = substitute;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public UserDto getDelegator() {
        return delegator;
    }

    public void setDelegator(UserDto delegator) {
        this.delegator = delegator;
    }

    public UserDto getSubstitute() {
        return substitute;
    }

    public void setSubstitute(UserDto substitute) {
        this.substitute = substitute;
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
}
