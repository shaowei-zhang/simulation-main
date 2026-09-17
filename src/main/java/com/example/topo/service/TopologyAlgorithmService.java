package com.example.topo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.topo.domain.EnergyAlgorithm;
import com.example.topo.domain.EnergyAlgorithmCapability;
import com.example.topo.domain.EnergyFunctionModule;
import com.example.topo.domain.EnergyType;
import com.example.topo.domain.TopologyDocuments;
import com.example.topo.mapper.EnergyAlgorithmMapper;
import com.example.topo.mapper.EnergyAlgorithmCapabilityMapper;
import com.example.topo.mapper.EnergyFunctionModuleMapper;
import com.example.topo.mapper.EnergyTypeMapper;
import com.example.topo.mapper.TopologyDocumentsMapper;
import com.example.topo.vo.AlgorithmCapabilityVO;
import com.example.topo.vo.AlgorithmVO;
import com.example.topo.vo.FunctionModuleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopologyAlgorithmService {

        private final TopologyDocumentsMapper topologyDocumentsMapper;
        private final EnergyTypeMapper energyTypeMapper;
        private final EnergyAlgorithmMapper energyAlgorithmMapper;
        private final EnergyFunctionModuleMapper energyFunctionModuleMapper;
        private final EnergyAlgorithmCapabilityMapper capabilityMapper;

        public Map<String, Object> getAlgorithmsForTopology(Long topologyId) {
                TopologyDocuments topology = topologyDocumentsMapper.selectById(topologyId);
        if (topology == null) {
            return Map.of("status", "error", "code", "TOPOLOGY_NOT_FOUND", "message", "Topology not found");
        }

                return buildCatalog(topology);
        }

        public Map<String, Object> getAlgorithmsForTopology(String topologyName) {
                TopologyDocuments topology = topologyDocumentsMapper.selectOne(
                                new LambdaQueryWrapper<TopologyDocuments>()
                                                .eq(TopologyDocuments::getName, topologyName)
                );
                if (topology == null) {
                        return Map.of("status", "error", "code", "TOPOLOGY_NOT_FOUND", "message", "Topology not found");
                }
                return buildCatalog(topology);
        }

        private Map<String, Object> buildCatalog(TopologyDocuments topology) {

        EnergyType energyType = findEnergyType(topology.getType());
        if (energyType == null) {
            return Map.of("status", "error", "code", "ENERGY_TYPE_NOT_FOUND", "message", "未找到当前拓扑的能源类型");
        }

        List<EnergyAlgorithm> algorithms = energyAlgorithmMapper.selectList(
                new LambdaQueryWrapper<EnergyAlgorithm>()
                        .eq(EnergyAlgorithm::getEnergyTypeId, energyType.getId())
                        .eq(EnergyAlgorithm::getEnabled, true)
                        .orderByAsc(EnergyAlgorithm::getSortOrder)
                        .orderByAsc(EnergyAlgorithm::getId)
        );
        Set<Long> moduleIds = algorithms.stream()
                .map(EnergyAlgorithm::getFunctionModuleId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        List<EnergyFunctionModule> modules = moduleIds.isEmpty()
                ? List.of()
                : energyFunctionModuleMapper.selectList(
                                new LambdaQueryWrapper<EnergyFunctionModule>()
                                        .in(EnergyFunctionModule::getId, moduleIds)
                                        .eq(EnergyFunctionModule::getEnabled, true)
                        );
        Set<Long> algorithmIds = algorithms.stream().map(EnergyAlgorithm::getId).collect(Collectors.toSet());
        Map<Long, List<EnergyAlgorithmCapability>> capabilitiesByAlgorithm = algorithmIds.isEmpty()
                ? Map.of()
                : capabilityMapper.selectList(
                                new LambdaQueryWrapper<EnergyAlgorithmCapability>()
                                        .in(EnergyAlgorithmCapability::getAlgorithmId, algorithmIds)
                                        .eq(EnergyAlgorithmCapability::getEnabled, true)
                                        .orderByAsc(EnergyAlgorithmCapability::getSortOrder)
                                        .orderByAsc(EnergyAlgorithmCapability::getId)
                        ).stream()
                        .collect(Collectors.groupingBy(EnergyAlgorithmCapability::getAlgorithmId));

        Map<Long, List<EnergyAlgorithm>> algorithmsByModule = algorithms.stream()
                .filter(item -> modules.stream().anyMatch(module -> module.getId().equals(item.getFunctionModuleId())))
                .collect(Collectors.groupingBy(EnergyAlgorithm::getFunctionModuleId));

        List<FunctionModuleVO> functionModules = modules.stream()
                .sorted(Comparator.comparing(EnergyFunctionModule::getSortOrder,
                        Comparator.nullsLast(Integer::compareTo)))
                .map(module -> {
                    FunctionModuleVO moduleData = new FunctionModuleVO();
                    moduleData.setId(module.getId());
                    moduleData.setModuleCode(module.getModuleCode());
                    moduleData.setModuleNameZh(module.getModuleNameZh());
                    moduleData.setModuleNameEn(module.getModuleNameEn());
                    moduleData.setIcon(module.getIcon());
                    moduleData.setAlgorithms(algorithmsByModule.getOrDefault(module.getId(), List.of())
                            .stream().map(algorithm -> toAlgorithmData(algorithm,
                                    capabilitiesByAlgorithm.getOrDefault(algorithm.getId(), List.of())))
                            .toList());
                    return moduleData;
                }).toList();

        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("topologyId", topology.getId());
        result.put("topologyName", topology.getName());
        result.put("energyType", topology.getType());
        result.put("energyTypeCode", energyType.getTypeCode());
        result.put("functionModules", functionModules);
        return result;
    }

    private EnergyType findEnergyType(String topologyType) {
        if (topologyType == null || topologyType.isBlank()) {
            return null;
        }
        String normalized = topologyType.trim();
        return energyTypeMapper.selectList(null).stream()
                .filter(item -> Boolean.TRUE.equals(item.getEnabled()))
                .filter(item -> normalized.equalsIgnoreCase(item.getTypeCode())
                        || normalized.equalsIgnoreCase(item.getTypeNameZh())
                        || normalized.equalsIgnoreCase(item.getTypeNameEn()))
                .findFirst()
                .orElse(null);
    }

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