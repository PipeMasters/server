package com.pipemasters.server.dto;

public class TrainDto {
    private Long trainNumber;
    private String routeMessage;
    private Integer consistCount;
    private String chief;

    public TrainDto(Long trainNumber, String routeMessage, Integer consistCount, String chief) {
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