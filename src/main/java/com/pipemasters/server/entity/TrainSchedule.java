package com.pipemasters.server.entity;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "train_schedule",
        indexes = {@Index(columnList = "train_number")})
public class TrainSchedule {
    @Column(nullable = false, unique = true)
    private Long trainNumber;

    @Column(nullable = false, length = 128)
    private String category;

    @Column(nullable = false, length = 128)
    private String departureStation;

    @Column(nullable = false, length = 128)
    private String arrivalStation;

    @Column(length = 256)
    private String customName;

    @Column(length = 512)
    private String railwayInfo; // ЖД АДМИНИСТРАЦИЯ, ПЕРЕВОЗЧИК, ФИЛИАЛ

    private Duration travelTime;

    private LocalDateTime departureTime;

    private LocalDateTime arrivalTime;

    private boolean isFirm;

    @Column(length = 128)
    private String periodicity;

    @Column(length = 128)
    private String seasonality;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_train_id")
    private TrainSchedule pairTrain;

    protected TrainSchedule() {
    }

    public TrainSchedule(Long trainNumber, String category, String departureStation, String arrivalStation,
                         String customName, String railwayInfo, Duration travelTime,
                         LocalDateTime departureTime, LocalDateTime arrivalTime,
                         boolean isFirm, String periodicity, String seasonality,
                         TrainSchedule pairTrain) {
        this.trainNumber = trainNumber;
        this.category = category;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.customName = customName;
        this.railwayInfo = railwayInfo;
        this.travelTime = travelTime;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.isFirm = isFirm;
        this.periodicity = periodicity;
        this.seasonality = seasonality;
        this.pairTrain = pairTrain;
    }

    public Long getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(Long trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDepartureStation() {
        return departureStation;
    }

    public void setDepartureStation(String departureStation) {
        this.departureStation = departureStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getRailwayInfo() {
        return railwayInfo;
    }

    public void setRailwayInfo(String railwayInfo) {
        this.railwayInfo = railwayInfo;
    }

    public Duration getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(Duration travelTime) {
        this.travelTime = travelTime;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public boolean isFirm() {
        return isFirm;
    }

    public void setFirm(boolean firm) {
        isFirm = firm;
    }

    public String getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(String periodicity) {
        this.periodicity = periodicity;
    }

    public String getSeasonality() {
        return seasonality;
    }

    public void setSeasonality(String seasonality) {
        this.seasonality = seasonality;
    }

    public TrainSchedule getPairTrain() {
        return pairTrain;
    }

    public void setPairTrain(TrainSchedule pairTrain) {
        this.pairTrain = pairTrain;
    }
}