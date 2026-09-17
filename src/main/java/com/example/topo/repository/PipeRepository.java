package com.example.topo.repository;

import com.example.topo.entity.PipeEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.Optional;

public interface PipeRepository extends BaseMapper<PipeEntity> {
    default Optional<PipeEntity> findByPipeCode(String pipeCode) {
        return Optional.ofNullable(selectOne(new QueryWrapper<PipeEntity>()
                .eq("pipe_code", pipeCode)));
    }
}
