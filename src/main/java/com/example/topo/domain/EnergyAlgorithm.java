package com.example.topo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 
 * @TableName energy_algorithm
 */
@TableName(value ="energy_algorithm")
@Data
public class EnergyAlgorithm implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private String algorithmCode;

    /**
     * 
     */
    private String algorithmNameEn;

    /**
     * 
     */
    private String algorithmNameZh;

    /**
     * 
     */
    private Long functionModuleId;

    /**
     * 
     */
    private Long energyTypeId;

    /**
     * 
     */
    private String algorithmType;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private String algorithmMethod;

    /**
     * 
     */
    private String openSourceProject;

    /**
     * 
     */
    private String githubUrl;

    /**
     * 
     */
    private String language;

    /**
     * 
     */
    private Boolean topologyRequired;

    /**
     * 
     */
    private Boolean topologyCalculation;

    /**
     * 
     */
    private Integer sortOrder;

    /**
     * 
     */
    private Boolean enabled;

    /**
     * 
     */
    private Date createdAt;

    /**
     * 
     */
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}