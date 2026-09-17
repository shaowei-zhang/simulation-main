package com.example.topo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("energy_algorithm_capability")
@Data
public class EnergyAlgorithmCapability implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long algorithmId;
    private String capabilityCode;
    private String capabilityNameZh;
    private String capabilityNameEn;
    private String capabilityType;
    private String description;
    private String calculationMethod;
    private Boolean topologyRequired;
    private Boolean topologyCalculation;
    private Integer sortOrder;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}