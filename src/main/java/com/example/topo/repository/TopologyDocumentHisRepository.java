package com.example.topo.repository;

import com.example.topo.entity.TopologyDocumentHisEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

public interface TopologyDocumentHisRepository extends BaseMapper<TopologyDocumentHisEntity> {
    default List<TopologyDocumentHisEntity> findAllByNameOrderBySavedAtDesc(String name) {
        return selectList(new QueryWrapper<TopologyDocumentHisEntity>()
                .eq("name", name)
                .orderByDesc("saved_at"));
    }

    default List<TopologyDocumentHisEntity> findAllByCampusOrderBySavedAtDesc(String campus) {
        return selectList(new QueryWrapper<TopologyDocumentHisEntity>()
                .eq("campus", campus)
                .orderByDesc("saved_at"));
    }

    default List<TopologyDocumentHisEntity> findAllByOrderBySavedAtDesc() {
        return selectList(new QueryWrapper<TopologyDocumentHisEntity>()
                .orderByDesc("saved_at"));
    }
}
