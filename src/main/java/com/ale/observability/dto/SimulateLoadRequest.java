package com.ale.observability.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulateLoadRequest {

    @Min(1)
    @Max(200)
    private int totalRequests = 30;
}