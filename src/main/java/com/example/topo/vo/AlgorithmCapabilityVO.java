package com.example.topo.vo;

import lombok.Data;

@Data
public class AlgorithmCapabilityVO {
    private Long id;
    private String capabilityCode;
    private String capabilityNameZh;
    private String capabilityNameEn;
    private String capabilityType;
    private String description;
    private String calculationMethod;
    private Boolean topologyRequired;
    private Boolean topologyCalculation;
    private Integer sortOrder;
}