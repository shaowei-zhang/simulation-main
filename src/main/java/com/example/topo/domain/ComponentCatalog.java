package com.example.topo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName component_catalog
 */
@TableName(value ="component_catalog")
@Data
public class ComponentCatalog implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String catalogJson;

    /**
     * 
     */
    private String category;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private String kind;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}