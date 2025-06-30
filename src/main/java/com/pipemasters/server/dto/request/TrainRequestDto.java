package com.pipemasters.server.dto.request;

import com.pipemasters.server.dto.BaseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TrainRequestDto extends BaseDto {
    @NotNull(message = "Train number cannot be null")
    private Long trainNumber;
    @NotBlank(message = "Route message cannot be blank")
    private String routeMessage;
    @Min(value = 1, message = "Consist count must be at least 1")
    private Integer consistCount;
    @NotBlank(message = "Chief name cannot be blank")
    private String chief;

    public TrainRequestDto(Long trainNumber, String routeMessage, Integer consistCount, String chief) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chief = chief;
    }

    public TrainRequestDto() {
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