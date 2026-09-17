package com.example.topo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.topo.domain.*;
import com.example.topo.mapper.*;
import com.example.topo.service.TopologyAlgorithmService;
import com.example.topo.vo.AlgorithmCapabilityVO;
import com.example.topo.vo.AlgorithmVO;
import com.example.topo.vo.FunctionModuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 算法能力目录服务实现
 * <p>
 * 根据拓扑文档的能源类型，从数据库关联查询出：
 * 能源类型 → 功能模块 → 算法 → 算法能力（capability）
 * <p>
 * 供前端算法菜单渲染使用，也供 Python 调度器做算法路由查询。
 */
@Service
@RequiredArgsConstructor
public class TopologyAlgorithmServiceImpl implements TopologyAlgorithmService {

    private final TopologyDocumentsMapper topologyDocumentsMapper;
    private final EnergyTypeMapper energyTypeMapper;
    private final EnergyAlgorithmMapper energyAlgorithmMapper;
    private final EnergyFunctionModuleMapper energyFunctionModuleMapper;
    private final EnergyAlgorithmCapabilityMapper energyAlgorithmCapabilityMapper;

    // =============================================================
    // 按拓扑 id 查询算法目录
    // =============================================================
    @Override
    public Map<String, Object> getAlgorithmsForTopology(Long topologyId) {
        TopologyDocuments topology = topologyDocumentsMapper.selectById(topologyId);
        if (topology == null) {
            return Map.of(
                    "status", "error",
                    "code", "TOPOLOGY_NOT_FOUND",
                    "message", "Topology not found"
            );
        }
        return buildCatalog(topology);
    }

    // =============================================================
    // 按拓扑名称查询算法目录
    // =============================================================
    @Override
    public Map<String, Object> getAlgorithmsForTopology(String topologyName) {
        if (topologyName == null || topologyName.isBlank()) {
            return Map.of(
                    "status", "error",
                    "code", "TOPOLOGY_NOT_FOUND",
                    "message", "Topology name is required"
            );
        }

        TopologyDocuments topology = topologyDocumentsMapper.selectOne(
                new LambdaQueryWrapper<TopologyDocuments>()
                        .eq(TopologyDocuments::getName, topologyName.trim())
                        .last("LIMIT 1")
        );
        if (topology == null) {
            return Map.of(
                    "status", "error",
                    "code", "TOPOLOGY_NOT_FOUND",
                    "message", "Topology not found"
            );
        }
        return buildCatalog(topology);
    }

    // =============================================================
    // 组装算法目录：能源类型 → 功能模块 → 算法 → 能力
    // =============================================================
    private Map<String, Object> buildCatalog(TopologyDocuments topology) {

        EnergyType energyType = findEnergyType(topology.getType());
        if (energyType == null) {
            return Map.of(
                    "status", "error",
                    "code", "ENERGY_TYPE_NOT_FOUND",
                    "message", "未找到当前拓扑的能源类型"
            );
        }

        // 查询当前能源类型下所有启用的算法
        List<EnergyAlgorithm> algorithms = energyAlgorithmMapper.selectList(
                new LambdaQueryWrapper<EnergyAlgorithm>()
                        .eq(EnergyAlgorithm::getEnergyTypeId, energyType.getId())
                        .eq(EnergyAlgorithm::getEnabled, true)
                        .orderByAsc(EnergyAlgorithm::getSortOrder)
                        .orderByAsc(EnergyAlgorithm::getId)
        );

        // 收集关联的功能模块 id
        Set<Long> moduleIds = algorithms.stream()
                .map(EnergyAlgorithm::getFunctionModuleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<EnergyFunctionModule> modules = moduleIds.isEmpty()
                ? List.of()
                : energyFunctionModuleMapper.selectList(
                new LambdaQueryWrapper<EnergyFunctionModule>()
                        .in(EnergyFunctionModule::getId, moduleIds)
                        .eq(EnergyFunctionModule::getEnabled, true)
        );

        // 批量查询所有算法能力，按 algorithmId 分组
        Set<Long> algorithmIds = algorithms.stream()
                .map(EnergyAlgorithm::getId)
                .collect(Collectors.toSet());

        Map<Long, List<EnergyAlgorithmCapability>> capabilitiesByAlgorithm = algorithmIds.isEmpty()
                ? Map.of()
                : energyAlgorithmCapabilityMapper.selectList(
                        new LambdaQueryWrapper<EnergyAlgorithmCapability>()
                                .in(EnergyAlgorithmCapability::getAlgorithmId, algorithmIds)
                                .eq(EnergyAlgorithmCapability::getEnabled, true)
                                .orderByAsc(EnergyAlgorithmCapability::getSortOrder)
                                .orderByAsc(EnergyAlgorithmCapability::getId)
                ).stream()
                .collect(Collectors.groupingBy(EnergyAlgorithmCapability::getAlgorithmId));

        // 按功能模块分组算法（只保留有对应模块的算法）
        Set<Long> validModuleIds = modules.stream()
                .map(EnergyFunctionModule::getId)
                .collect(Collectors.toSet());

        Map<Long, List<EnergyAlgorithm>> algorithmsByModule = algorithms.stream()
                .filter(item -> validModuleIds.contains(item.getFunctionModuleId()))
                .collect(Collectors.groupingBy(EnergyAlgorithm::getFunctionModuleId));

        // 组装模块 VO
        List<FunctionModuleVO> functionModules = modules.stream()
                .sorted(Comparator.comparing(
                        EnergyFunctionModule::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(module -> {
                    FunctionModuleVO moduleData = new FunctionModuleVO();
                    moduleData.setId(module.getId());
                    moduleData.setModuleCode(module.getModuleCode());
                    moduleData.setModuleNameZh(module.getModuleNameZh());
                    moduleData.setModuleNameEn(module.getModuleNameEn());
                    moduleData.setIcon(module.getIcon());
                    moduleData.setAlgorithms(
                            algorithmsByModule.getOrDefault(module.getId(), List.of())
                                    .stream()
                                    .map(algorithm -> toAlgorithmData(
                                            algorithm,
                                            capabilitiesByAlgorithm.getOrDefault(algorithm.getId(), List.of())
                                    ))
                                    .toList()
                    );
                    return moduleData;
                })
                .toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topologyId", topology.getId());
        result.put("topologyName", topology.getName());
        result.put("energyType", topology.getType());
        result.put("energyTypeCode", energyType.getTypeCode());
        result.put("functionModules", functionModules);
        return result;
    }

    // =============================================================
    // 根据拓扑 type 匹配能源类型（SQL 条件查询，支持大小写不敏感）
    // =============================================================
    private EnergyType findEnergyType(String topologyType) {
        if (topologyType == null || topologyType.isBlank()) {
            return null;
        }
        String normalized = topologyType.trim();

        // 用 LOWER 保证大小写不敏感，不依赖数据库排序规则
        return energyTypeMapper.selectOne(
                new LambdaQueryWrapper<EnergyType>()
                        .eq(EnergyType::getEnabled, true)
                        .and(w -> w
                                .apply("LOWER(type_code) = {0}", normalized.toLowerCase())
                                .or()
                                .apply("LOWER(type_name_zh) = {0}", normalized.toLowerCase())
                                .or()
                                .apply("LOWER(type_name_en) = {0}", normalized.toLowerCase())
                        )
                        .last("LIMIT 1")
        );
    }

    // =============================================================
    // 实体 → VO
    // =============================================================
    private AlgorithmVO toAlgorithmData(EnergyAlgorithm algorithm,
                                        List<EnergyAlgorithmCapability> capabilities) {
        AlgorithmVO data = new AlgorithmVO();
        data.setId(algorithm.getId());
        data.setAlgorithmCode(algorithm.getAlgorithmCode());
        data.setAlgorithmNameZh(algorithm.getAlgorithmNameZh());
        data.setAlgorithmNameEn(algorithm.getAlgorithmNameEn());
        data.setAlgorithmType(algorithm.getAlgorithmType());
        data.setDescription(algorithm.getDescription());
        data.setAlgorithmMethod(algorithm.getAlgorithmMethod());
        data.setOpenSourceProject(algorithm.getOpenSourceProject());
        data.setGithubUrl(algorithm.getGithubUrl());
        data.setLanguage(algorithm.getLanguage());
        data.setTopologyRequired(algorithm.getTopologyRequired());
        data.setTopologyCalculation(algorithm.getTopologyCalculation());
        data.setCapabilities(capabilities.stream().map(this::toCapabilityData).toList());
        return data;
    }

    private AlgorithmCapabilityVO toCapabilityData(EnergyAlgorithmCapability capability) {
        AlgorithmCapabilityVO data = new AlgorithmCapabilityVO();
        data.setId(capability.getId());
        data.setCapabilityCode(capability.getCapabilityCode());
        data.setCapabilityNameZh(capability.getCapabilityNameZh());
        data.setCapabilityNameEn(capability.getCapabilityNameEn());
        data.setCapabilityType(capability.getCapabilityType());
        data.setDescription(capability.getDescription());
        data.setCalculationMethod(capability.getCalculationMethod());
        data.setTopologyRequired(capability.getTopologyRequired());
        data.setTopologyCalculation(capability.getTopologyCalculation());
        data.setSortOrder(capability.getSortOrder());
        return data;
    }
}