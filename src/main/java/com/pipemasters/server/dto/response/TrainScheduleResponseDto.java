package com.pipemasters.server.dto.response;

import com.pipemasters.server.dto.BaseDto;
import com.pipemasters.server.entity.TrainSchedule;

import java.time.Duration;
import java.time.LocalTime;

public class TrainScheduleResponseDto extends BaseDto {
    private String trainNumber;
    private String category;
    private String departureStation;
    private String arrivalStation;
    private String customName;
    private String railwayInfo;
    private Duration travelTime;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private boolean isFirm;
    private String periodicity;
    private String seasonality;
    private Long pairTrainId;

    public TrainScheduleResponseDto() {
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(String trainNumber) {
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

    public Long getPairTrainId() {
        return pairTrainId;
    }

    public void setPairTrainId(Long pairTrainId) {
        this.pairTrainId = pairTrainId;
    }
}
