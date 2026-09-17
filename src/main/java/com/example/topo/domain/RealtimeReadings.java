package com.example.topo.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 
 * @TableName realtime_readings
 */
@TableName(value ="realtime_readings")
@Data
public class RealtimeReadings implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private LocalDateTime collectedAt;

    /**
     * 
     */
    private String deviceCode;

    /**
     * 
     */
    private String metricName;

    /**
     * 
     */
    private String source;

    /**
     * 
     */
    private String unit;

    /**
     * 
     */
    private BigDecimal value;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}