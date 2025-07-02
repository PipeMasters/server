package com.pipemasters.server.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pipemasters.server.dto.BaseDto;
import com.pipemasters.server.entity.User;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrainResponseDto extends BaseDto {
    private Long trainNumber;
    private String routeMessage;
    private Integer consistCount;
    private Long chiefId;
    private Long branchId;

    public TrainResponseDto(Long trainNumber, String routeMessage, Integer consistCount, Long chiefId, Long branchId) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chiefId = chiefId;
        this.branchId = branchId;
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

    public Long getChiefId() {
        return chiefId;
    }

    public void setChiefId(Long chiefId) {
        this.chiefId = chiefId;
    }
    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    @Override
    public String toString() {
        return "TrainDto{" +
                "trainNumber=" + trainNumber +
                ", routeMessage='" + routeMessage + '\'' +
                ", consistCount=" + consistCount +
                ", chiefId=" + chiefId +
                ", branchId=" + branchId +
                '}';
    }
}