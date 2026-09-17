package com.example.topo.vo;

import lombok.Data;

import java.util.List;

@Data
public class AlgorithmVO {
    private Long id;
    private String algorithmCode;
    private String algorithmNameZh;
    private String algorithmNameEn;
    private String algorithmType;
    private String description;
    private String algorithmMethod;
    private String openSourceProject;
    private String githubUrl;
    private String language;
    private Boolean topologyRequired;
    private Boolean topologyCalculation;
    private List<AlgorithmCapabilityVO> capabilities;
}