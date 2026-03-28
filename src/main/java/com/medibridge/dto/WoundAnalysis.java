package com.medibridge.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class WoundAnalysis {
    private String woundType;
    private String severity;
    private Boolean needsStitches;
    private String bleeding;
    private String infectionRisk;
    private List<String> recommendations;
}
