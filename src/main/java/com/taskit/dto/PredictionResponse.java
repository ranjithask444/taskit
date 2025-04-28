package com.taskit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PredictionResponse {
    @JsonProperty(value = "assigned_employee_id")
    private Long assignedEmployeeId;

    @JsonProperty(value = "assigned_employee_name")
    private String assignedEmployeeName;

    @JsonProperty(value = "assigned_employee_skills")
    private List<String> assignedEmployeeSkills;

    @JsonProperty(value = "available_bandwidth")
    private Integer availableBandwidth;
}
