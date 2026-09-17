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
 * @TableName topology_documents_his
 */
@TableName(value ="topology_documents_his")
@Data
public class TopologyDocumentsHis implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private String campus;

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
    private LocalDateTime savedAt;

    /**
     * 
     */
    private String type;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}