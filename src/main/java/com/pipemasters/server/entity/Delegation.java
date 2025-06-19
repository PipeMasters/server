package com.pipemasters.server.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "delegations")
public class Delegation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "delegator_id")
    private User delegator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "substitute_id")
    private User substitute;

    private LocalDate fromDate;
    private LocalDate toDate;

    public Delegation(User delegator, User substitute, LocalDate fromDate, LocalDate toDate) {
        this.delegator = delegator;
        this.substitute = substitute;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    protected Delegation() {
    }

    public User getDelegator() {
        return delegator;
    }

    public void setDelegator(User delegator) {
        this.delegator = delegator;
    }

    public User getSubstitute() {
        return substitute;
    }

    public void setSubstitute(User substitute) {
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

    @Override
    public String toString() {
        return "Delegation{" +
                "delegator=" + delegator +
                ", substitute=" + substitute +
                ", fromDate=" + fromDate +
                ", toDate=" + toDate +
                '}';
    }
}