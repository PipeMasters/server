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
    @NotNull(message = "Chief name cannot be null")
    private Long chiefId;
    @NotNull(message = "Branch ID cannot be null")
    private Long branchId;

    public TrainRequestDto(Long trainNumber, String routeMessage, Integer consistCount, Long chiefId, Long branchId) {
        this.trainNumber = trainNumber;
        this.routeMessage = routeMessage;
        this.consistCount = consistCount;
        this.chiefId = chiefId;
        this.branchId = branchId;
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
}