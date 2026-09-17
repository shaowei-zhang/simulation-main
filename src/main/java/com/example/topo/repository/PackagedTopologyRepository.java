package com.example.topo.repository;

import com.example.topo.entity.PackagedTopologyEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Optional;

public interface PackagedTopologyRepository extends BaseMapper<PackagedTopologyEntity> {
    default Optional<PackagedTopologyEntity> findByName(String name) {
        return Optional.ofNullable(selectOne(new QueryWrapper<PackagedTopologyEntity>()
                .eq("name", name)));
    }

    default List<PackagedTopologyEntity> findAllByOrderByNameAsc() {
        return selectList(new QueryWrapper<PackagedTopologyEntity>()
                .orderByAsc("name"));
    }
}
