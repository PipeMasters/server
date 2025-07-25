package com.pipemasters.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "trains",
        indexes = {@Index(columnList = "train_number")})
public class Train extends BaseEntity {

    @Column(nullable = false)
    private Long trainNumber;           // № поезда

    @Column(length = 256, nullable = false)
    private String routeMessage;        // например «Москва — Сочи»

    private Integer consistCount;       // кол-во составов

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "chief_id", nullable = false)
    private User chief;                 // Начальник поезда

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;            // принадлежность к филиалу

    public Train(Long trainNumber, String routeMessage, Integer consistCount, User chief, Branch branch) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chief = chief;
        this.branch = branch;
    }

    protected Train() {
    }

    public Long getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(Long trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getRouteMessage() {
        return routeMessage;
    }

    public void setRouteMessage(String routeMessage) {
        this.routeMessage = routeMessage;
    }

    public Integer getConsistCount() {
        return consistCount;
    }

    public void setConsistCount(Integer consistCount) {
        this.consistCount = consistCount;
    }

    public User getChief() {
        return chief;
    }

    public void setChief(User chief) {
        this.chief = chief;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}