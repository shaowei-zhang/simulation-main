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
 * @TableName energy_function_module
 */
@TableName(value ="energy_function_module")
@Data
public class EnergyFunctionModule implements Serializable {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 
     */
    private String moduleCode;

    /**
     * 
     */
    private String moduleNameZh;

    /**
     * 
     */
    private String moduleNameEn;

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