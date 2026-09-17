package com.example.topo.vo;

import lombok.Data;

import java.util.List;

@Data
public class FunctionModuleVO {
    private Long id;
    private String moduleCode;
    private String moduleNameZh;
    private String moduleNameEn;
    private String icon;
    private List<AlgorithmVO> algorithms;
}