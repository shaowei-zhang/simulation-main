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
 * @TableName topology_documents
 */
@TableName(value ="topology_documents")
@Data
public class TopologyDocuments implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String description;

    /**
     * 
     */
    private String diagramJson;

    /**
     * 
     */
    private String name;

    /**
     * 
     */
    private LocalDateTime updatedAt;

    /**
     * 
     */
    private String campus;

    /**
     * 热能、电能、混合
     */
    private String type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}