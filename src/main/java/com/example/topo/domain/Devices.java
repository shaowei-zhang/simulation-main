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
 * @TableName devices
 */
@TableName(value ="devices")
@Data
public class Devices implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String category;

    /**
     * 
     */
    private String configJson;

    /**
     * 
     */
    private String deviceCode;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private String remarks;

    /**
     * 
     */
    private String type;

    /**
     * 
     */
    private String unit;

    /**
     * 
     */
    private BigDecimal value;

    /**
     * 
     */
    private BigDecimal x;

    /**
     * 
     */
    private BigDecimal y;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}