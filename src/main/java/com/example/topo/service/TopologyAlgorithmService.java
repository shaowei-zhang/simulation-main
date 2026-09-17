package com.example.topo.service;


import java.util.Map;

public interface TopologyAlgorithmService {

    /**
     * 按拓扑 id 查询算法目录
     */
    Map<String, Object> getAlgorithmsForTopology(Long topologyId);

    /**
     * 按拓扑名称查询算法目录
     */
    Map<String, Object> getAlgorithmsForTopology(String topologyName);
}