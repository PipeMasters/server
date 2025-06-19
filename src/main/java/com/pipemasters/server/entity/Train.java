package com.pipemasters.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "trains",
        indexes = {@Index(columnList = "trainNumber")})
public class Train extends BaseEntity {

    @Column(nullable = false)
    private Long trainNumber;           // № поезда

    @Column(length = 256, nullable = false)
    private String routeMessage;        // например «Москва — Сочи»

    private Integer consistCount;       // кол-во составов
    @Column(nullable = false)
    private String chief;               // Ф. И. О. начальника

    public Train(Long trainNumber, String routeMessage, Integer consistCount, String chief) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chief = chief;
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

    public String getChief() {
        return chief;
    }

    public void setChief(String chief) {
        this.chief = chief;
    }
}