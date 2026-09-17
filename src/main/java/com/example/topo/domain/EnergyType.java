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
 * @TableName energy_type
 */
@TableName(value ="energy_type")
@Data
public class EnergyType implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private String typeCode;

    /**
     * 
     */
    private String typeNameZh;

    /**
     * 
     */
    private String typeNameEn;

    /**
     * 
     */
    private String icon;

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