package com.example.topo.repository;

import com.example.topo.entity.DeviceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Optional;

public interface DeviceRepository extends BaseMapper<DeviceEntity> {
    default Optional<DeviceEntity> findByDeviceCode(String deviceCode) {
        return Optional.ofNullable(selectOne(new QueryWrapper<DeviceEntity>()
                .eq("device_code", deviceCode)));
    }
}
