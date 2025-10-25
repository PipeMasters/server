package com.pipemasters.server.dto.request.update;

import jakarta.validation.constraints.Size;

import java.time.Duration;
import java.time.LocalTime;

public class TrainScheduleUpdateDto {

    @Size(max = 256, message = "Category must not exceed 256 characters")
    private String category;

    @Size(max = 128, message = "Departure station must not exceed 128 characters")
    private String departureStation;

    @Size(max = 128, message = "Arrival station must not exceed 128 characters")
    private String arrivalStation;

    @Size(max = 256, message = "Custom name must not exceed 256 characters")
    private String customName;

    @Size(max = 512, message = "Railway info must not exceed 512 characters")
    private String railwayInfo;

    private Duration travelTime;

    private LocalTime departureTime;

    private LocalTime arrivalTime;

    private Boolean isFirm;

    @Size(max = 128, message = "Periodicity must not exceed 128 characters")
    private String periodicity;

    @Size(max = 128, message = "Seasonality must not exceed 128 characters")
    private String seasonality;

    private Long pairTrainId;

    public TrainScheduleUpdateDto() {
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

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Boolean getFirm() {
        return isFirm;
    }

    public void setFirm(Boolean firm) {
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

    public Long getPairTrainId() {
        return pairTrainId;
    }

    public void setPairTrainId(Long pairTrainId) {
        this.pairTrainId = pairTrainId;
    }
}
