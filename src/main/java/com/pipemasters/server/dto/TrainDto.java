package com.pipemasters.server.dto;

import com.pipemasters.server.entity.User;

import java.time.LocalDate;

public class TrainDto extends BaseDto {
    private Long trainNumber;
    private String routeMessage;
    private Integer consistCount;
    private Long chiefId;

    public TrainDto(Long trainNumber, String routeMessage, Integer consistCount, Long chiefId) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chiefId = chiefId;
    }

    public TrainDto() {
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

    public Long getChiefId() {
        return chiefId;
    }

    public void setChiefId(Long chiefId) {
        this.chiefId = chiefId;
    }

    @Override
    public String toString() {
        return "TrainDto{" +
                "trainNumber=" + trainNumber +
                ", routeMessage='" + routeMessage + '\'' +
                ", consistCount=" + consistCount +
                ", chiefId=" + chiefId +
                '}';
    }
}