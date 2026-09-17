package com.example.topo.repository;

import com.example.topo.entity.ComponentCatalogEntity;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Optional;

public interface ComponentCatalogRepository extends BaseMapper<ComponentCatalogEntity> {
    default Optional<ComponentCatalogEntity> findByKind(String kind) {
        return Optional.ofNullable(selectOne(new QueryWrapper<ComponentCatalogEntity>()
                .eq("kind", kind)));
    }

    default Optional<ComponentCatalogEntity> findByKindIgnoreCase(String kind) {
        return Optional.ofNullable(selectOne(new QueryWrapper<ComponentCatalogEntity>()
                .apply("LOWER(kind) = LOWER({0})", kind)));
    }

    default List<ComponentCatalogEntity> findAllByOrderByKindAsc() {
        return selectList(new QueryWrapper<ComponentCatalogEntity>()
                .orderByAsc("kind"));
    }
}
