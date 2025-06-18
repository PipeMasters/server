package com.pipemasters.server.dto;

import com.pipemasters.server.entity.User;

import java.time.LocalDate;

public class DelegationDto extends BaseDto{
    private UserDto delegator;
    private UserDto substitute;
    private LocalDate fromDate;
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
