package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainResponseDto extends BaseDto {
    private Long trainNumber;
    private String routeMessage;
    private Integer consistCount;
    private String chief;

    public TrainResponseDto(Long trainNumber, String routeMessage, Integer consistCount, String chief) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chief = chief;
    }

    public TrainResponseDto() {
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

    @Override
    public String toString() {
        return "TrainDTO{" +
                "trainNumber=" + trainNumber +
                ", routeMessage='" + routeMessage +
                ", consistCount=" + consistCount +
                ", chief='" + chief +
                '}';
    }
}