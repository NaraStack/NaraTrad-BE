package com.naratrad.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceChartDTO {
    private List<String> labels;        // Date labels (e.g., "Jan 1", "Jan 2")
    private List<Double> values;        // Portfolio values for each date
}