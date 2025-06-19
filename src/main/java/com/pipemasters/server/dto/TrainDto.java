package com.pipemasters.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainDto extends BaseDto{
    @NotNull(message = "TrainNumber cannot be empty")
    private Long trainNumber;
    @NotNull(message = "RouteMessage cannot be empty")
    private String routeMessage;
    @NotNull(message = "ConsistCount cannot be empty")
    @Min(value = 0, message = "ConsistCount can't be negative")
    private Integer consistCount;
    @NotNull(message = "Chief cannot be empty")
    private String chief;

    public TrainDto() {
    }

    public TrainDto( Long trainNumber, String routeMessage, Integer consistCount, String chief) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chief = chief;
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
