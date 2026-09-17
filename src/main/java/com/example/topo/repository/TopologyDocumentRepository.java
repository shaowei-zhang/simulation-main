package com.example.topo.repository;

import com.example.topo.entity.TopologyDocumentEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Optional;

public interface TopologyDocumentRepository extends BaseMapper<TopologyDocumentEntity> {
    default Optional<TopologyDocumentEntity> findByName(String name) {
        return Optional.ofNullable(selectOne(new QueryWrapper<TopologyDocumentEntity>()
                .eq("name", name)));
    }


    default List<TopologyDocumentEntity> findAllByOrderByNameAsc() {
        return selectList(new QueryWrapper<TopologyDocumentEntity>()
                .orderByAsc("name"));
    }

    default List<TopologyDocumentEntity> findAllByOrderByUpdatedAtDesc() {
        return selectList(new QueryWrapper<TopologyDocumentEntity>()
                .orderByDesc("updated_at"));
    }

    default List<TopologyDocumentEntity> findAllByCampusOrderByNameAsc(String campus) {
        return selectList(new QueryWrapper<TopologyDocumentEntity>()
                .eq("campus", campus)
                .orderByAsc("name"));
    }

    default List<TopologyDocumentEntity> findAllByCampusOrderByUpdatedAtDesc(String campus) {
        return selectList(new QueryWrapper<TopologyDocumentEntity>()
                .eq("campus", campus)
                .orderByDesc("updated_at"));
    }
}
