package com.example.topo.repository;

import com.example.topo.entity.RealtimeReadingEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.time.LocalDateTime;
import java.util.List;

public interface RealtimeReadingRepository extends BaseMapper<RealtimeReadingEntity> {
    default List<RealtimeReadingEntity> findTop20ByDeviceCodeOrderByCollectedAtDesc(String deviceCode) {
        return selectList(new QueryWrapper<RealtimeReadingEntity>()
                .eq("device_code", deviceCode)
                .orderByDesc("collected_at")
                .last("LIMIT 20"));
    }

    default List<RealtimeReadingEntity> findTop20ByOrderByCollectedAtDesc() {
        return selectList(new QueryWrapper<RealtimeReadingEntity>()
                .orderByDesc("collected_at")
                .last("LIMIT 20"));
    }

    default List<RealtimeReadingEntity> findByCollectedAtAfter(LocalDateTime startTime) {
        return selectList(new QueryWrapper<RealtimeReadingEntity>()
                .gt("collected_at", startTime));
    }
}
