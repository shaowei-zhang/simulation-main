package com.example.topo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 
 * @TableName pipes
 */
@TableName(value ="pipes")
@Data
public class Pipes implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String configJson;

    /**
     * 
     */
    private BigDecimal diameterM;

    /**
     * 
     */
    private BigDecimal flowRate;

    /**
     * 
     */
    private String fromDevice;

    /**
     * 
     */
    private BigDecimal inletTemp;

    /**
     * 
     */
    private BigDecimal lengthKm;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private BigDecimal outletTemp;

    /**
     * 
     */
    private String pipeCode;

    /**
     * 
     */
    private BigDecimal pressureDrop;

    /**
     * 
     */
    private String toDevice;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private String unit;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}