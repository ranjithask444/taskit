package com.taskit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PredictionResponse {
    @JsonProperty(value = "selected_user_id")
    private Long selectedEmployeeId;
}
