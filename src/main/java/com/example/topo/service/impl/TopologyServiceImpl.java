package com.example.topo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.topo.domain.ComponentCatalog;
import com.example.topo.domain.PackagedTopologies;
import com.example.topo.domain.TopologyDocuments;
import com.example.topo.domain.TopologyDocumentsHis;
import com.example.topo.mapper.*;
import com.example.topo.service.TopologyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TopologyServiceImpl implements TopologyService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==== 所有 Mapper ====
    private final ComponentCatalogMapper componentCatalogMapper;
    private final EnergyAlgorithmMapper energyAlgorithmMapper;
    private final EnergyAlgorithmCapabilityMapper energyAlgorithmCapabilityMapper;
    private final EnergyFunctionModuleMapper energyFunctionModuleMapper;
    private final EnergyTypeMapper energyTypeMapper;
    private final PackagedTopologiesMapper packagedTopologiesMapper;
    private final TopologyDocumentsMapper topologyDocumentsMapper;
    private final TopologyDocumentsHisMapper topologyDocumentsHisMapper;


    // =============================================================
    // 实时快照
    // =============================================================
    public Map<String, Object> getRealtimeSnapshot(String campus) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> diagram = getCurrentDiagram(campus);
        TopologyDocuments current = findCurrentTopology(campus);
        result.put("id", current == null ? null : current.getId());
        result.put("type", current == null ? null : current.getType());
        result.put("diagram", diagram);
        result.put("topologyName", current == null ? "" : current.getName());
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    // =============================================================
    // 拓扑目录
    // =============================================================
    public List<Map<String, Object>> getTopologyCatalog(String campus) {
        List<TopologyDocuments> entities;
        if (campus == null || campus.trim().isEmpty()) {
            entities = topologyDocumentsMapper.selectList(
                    new QueryWrapper<TopologyDocuments>().orderByAsc("name")
            );
        } else {
            entities = topologyDocumentsMapper.selectList(
                    new QueryWrapper<TopologyDocuments>()
                            .eq("campus", campus.trim())
                            .orderByAsc("name")
            );
        }
        return entities.stream()
                .map(this::toTopologySummary)
                .toList();
    }

    // =============================================================
    // 组件目录
    // =============================================================
    public List<Map<String, Object>> getComponentCatalog() {
        return componentCatalogMapper.selectList(
                        new QueryWrapper<ComponentCatalog>().orderByAsc("kind")
                ).stream()
                .map(this::toComponentCatalogMap)
                .toList();
    }

    @Override
    public List<Map<String, Object>> getPackagedTopologyCatalog() {
        return packagedTopologiesMapper.selectList(
                        new QueryWrapper<PackagedTopologies>().orderByAsc("name")
                ).stream()
                .map(this::toPackagedTopologiesMap)
                .toList();
    }

    @Override
    public Map<String, Object> savePackagedTopology(Map<String, Object> payload) {
        String name = sanitizeNameFromPayload(payload == null ? null : payload.get("name"));
        if (name.isBlank()) {
            return Map.of("status", "error", "message", "name is required");
        }

        Object entry = payload == null ? null : payload.get("entry");
        if (entry == null) {
            return Map.of("status", "error", "message", "entry is required");
        }

        String entryJson;
        try {
            entryJson = objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            return Map.of("status", "error", "message", "invalid entry json");
        }

        // 使用 QueryWrapper 按 name 查询
        PackagedTopologies entity = packagedTopologiesMapper.selectOne(
                new QueryWrapper<PackagedTopologies>().eq("name", name)
        );
        if (entity == null) {
            entity = new PackagedTopologies();
            entity.setName(name);
        }

        entity.setName(name);
        entity.setDescription(firstNonBlank(
                stringValue(payload.get("description")),
                entity.getDescription(),
                "Saved packaged topology"
        ));
        entity.setEntryJson(entryJson);
        entity.setUpdatedAt(LocalDateTime.now());

        if (entity.getId() == null) {
            packagedTopologiesMapper.insert(entity);
        } else {
            packagedTopologiesMapper.updateById(entity);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("name", entity.getName());
        result.put("updatedAt", entity.getUpdatedAt());
        return result;
    }

    @Override
    public Map<String, Object> deletePackagedTopology(String name) {
        String resolvedName = sanitizeNameValue(name);
        if (resolvedName.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        // 使用 QueryWrapper 按 name 查询
        PackagedTopologies entity = packagedTopologiesMapper.selectOne(
                new QueryWrapper<PackagedTopologies>().eq("name", resolvedName)
        );
        if (entity == null) {
            return Map.of("status", "error", "message", "Packaged topology not found");
        }

        packagedTopologiesMapper.deleteById(entity.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("deleted", true);
        response.put("name", resolvedName);
        return response;
    }

    // =============================================================
    // 打包拓扑目录
    // =============================================================
    public List<Map<String, Object>> getPackagedTopologiesCatalog() {
        return packagedTopologiesMapper.selectList(
                        new QueryWrapper<PackagedTopologies>().orderByAsc("name")
                ).stream()
                .map(this::toPackagedTopologiesMap)
                .toList();
    }

    // =============================================================
    // 保存打包拓扑
    // =============================================================
    public Map<String, Object> savePackagedTopologies(Map<String, Object> payload) {
        String name = sanitizeNameFromPayload(payload == null ? null : payload.get("name"));
        if (name.isBlank()) {
            return Map.of("status", "error", "message", "name is required");
        }

        Object entry = payload == null ? null : payload.get("entry");
        if (entry == null) {
            return Map.of("status", "error", "message", "entry is required");
        }

        String entryJson;
        try {
            entryJson = objectMapper.writeValueAsString(entry);
        } catch (JsonProcessingException e) {
            return Map.of("status", "error", "message", "invalid entry json");
        }

        PackagedTopologies entity = packagedTopologiesMapper.selectOne(
                new QueryWrapper<PackagedTopologies>().eq("name", name)
        );
        if (entity == null) {
            entity = new PackagedTopologies();
            entity.setName(name);
        }

        entity.setName(name);
        entity.setDescription(firstNonBlank(
                stringValue(payload.get("description")),
                entity.getDescription(),
                "Saved packaged topology"
        ));
        entity.setEntryJson(entryJson);
        entity.setUpdatedAt(LocalDateTime.now());

        if (entity.getId() == null) {
            packagedTopologiesMapper.insert(entity);
        } else {
            packagedTopologiesMapper.updateById(entity);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("name", entity.getName());
        result.put("updatedAt", entity.getUpdatedAt());
        return result;
    }

    // =============================================================
    // 删除打包拓扑
    // =============================================================
    public Map<String, Object> deletePackagedTopologies(String name) {
        String resolvedName = sanitizeNameValue(name);
        if (resolvedName.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        PackagedTopologies entity = packagedTopologiesMapper.selectOne(
                new QueryWrapper<PackagedTopologies>().eq("name", resolvedName)
        );
        if (entity == null) {
            return Map.of("status", "error", "message", "Packaged topology not found");
        }

        packagedTopologiesMapper.deleteById(entity.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("deleted", true);
        response.put("name", resolvedName);
        return response;
    }

    // =============================================================
    // 按 kind 查询组件
    // =============================================================
    public Map<String, Object> getComponentByKind(String kind) {
        String resolved = kind == null ? "" : kind.trim();
        if (resolved.isEmpty()) {
            return Map.of("status", "error", "message", "kind is required");
        }

        ComponentCatalog entity = componentCatalogMapper.selectOne(
                new QueryWrapper<ComponentCatalog>()
                        .eq("kind", resolved)
                        .last("LIMIT 1")
        );

        if (entity == null) {
            return Map.of("status", "not_found", "kind", resolved);
        }
        return toComponentCatalogMap(entity);
    }

    // =============================================================
    // 根据 ID 获取图
    // =============================================================
    public Map<String, Object> getDiagramByName(Long id) {
        if (id == null) {
            return emptyDiagram();
        }

        TopologyDocuments entity = topologyDocumentsMapper.selectById(id);

        if (entity == null || entity.getDiagramJson() == null || entity.getDiagramJson().isBlank()) {
            return emptyDiagram();
        }

        try {
            return objectMapper.readValue(entity.getDiagramJson(), Map.class);
        } catch (JsonProcessingException e) {
            return emptyDiagram();
        }
    }

    // =============================================================
    // 拓扑详情
    // =============================================================
    @Transactional
    public TopologyDocuments getTopoDetail(Long id) {
        return topologyDocumentsMapper.selectById(id);
    }

    // =============================================================
    // 保存拓扑文档
    // =============================================================
    @Transactional
    public Map<String, Object> saveTopologyDocument(Map<String, Object> payload) {

        Map<String, Object> diagramPayload = payload == null ? Map.of() : payload;

        // 1. id
        String id = sanitizeNameFromPayload(diagramPayload.get("id"));

        // 2. name
        String name = sanitizeNameFromPayload(diagramPayload.get("name"));
        if (name.isBlank()) {
            name = "default";
        }
        final String resolvedName = name;

        // 3. diagram
        Object rawDiagram = diagramPayload.getOrDefault("diagram", diagramPayload);
        String diagramJson;
        try {
            diagramJson = objectMapper.writeValueAsString(rawDiagram);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid diagram JSON payload", e);
        }

        // 4. type
        String type = sanitizeNameFromPayload(diagramPayload.get("type"));
        if (type.isBlank()) {
            type = "default";
        }

        // 5. campus
        String campusValue = stringValue(diagramPayload.get("campus"));

        // 6. description
        String description = firstNonBlank(
                stringValue(diagramPayload.get("description")),
                stringValue(diagramPayload.get("title")),
                "Saved editor topology"
        );

        // 7. 新增 / 更新
        TopologyDocuments entity;
        boolean created;

        if (StringUtils.isEmpty(id)) {
            // ---- 新增 ----
            Long count = topologyDocumentsMapper.selectCount(
                    new QueryWrapper<TopologyDocuments>().eq("name", resolvedName)
            );
            if (count != null && count > 0) {
                return errorResponse("101", "拓扑名称已存在，请使用其他名称", resolvedName);
            }

            entity = new TopologyDocuments();
            entity.setName(resolvedName);
            entity.setDescription(description);
            created = true;

        } else {
            // ---- 更新 ----
            Long documentId;
            try {
                documentId = Long.valueOf(id);
            } catch (NumberFormatException e) {
                return errorResponse("INVALID_ID", "无效的拓扑 ID：" + id, null);
            }

            entity = topologyDocumentsMapper.selectById(documentId);
            if (entity == null) {
                return errorResponse("TOPOLOGY_NOT_FOUND", "未找到对应的拓扑，ID：" + documentId, null);
            }

            TopologyDocuments sameName = topologyDocumentsMapper.selectOne(
                    new QueryWrapper<TopologyDocuments>().eq("name", resolvedName)
            );
            if (sameName != null && !Objects.equals(sameName.getId(), documentId)) {
                return errorResponse("101", "拓扑名称已存在，请使用其他名称", resolvedName);
            }

            created = false;
        }

        // 8. 更新字段
        entity.setName(resolvedName);
        entity.setDiagramJson(diagramJson);
        entity.setDescription(description);

        if (campusValue != null && !campusValue.trim().isEmpty()) {
            entity.setCampus(campusValue.trim());
        }
        entity.setType(type);
        entity.setUpdatedAt(LocalDateTime.now());

        // 9. 保存主表
        if (created) {
            topologyDocumentsMapper.insert(entity);
        } else {
            topologyDocumentsMapper.updateById(entity);
        }

        // 10. 保存历史表
        TopologyDocumentsHis history = new TopologyDocumentsHis();
        history.setName(entity.getName());
        history.setDiagramJson(entity.getDiagramJson());
        history.setDescription(entity.getDescription());
        history.setCampus(entity.getCampus());
        history.setType(entity.getType());
        history.setSavedAt(entity.getUpdatedAt());
        topologyDocumentsHisMapper.insert(history);

        // 11. 返回结果
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("created", created);
        response.put("id", entity.getId());
        response.put("name", entity.getName());
        response.put("type", entity.getType());
        response.put("campus", entity.getCampus());
        response.put("updatedAt", entity.getUpdatedAt());
        return response;
    }

    // =============================================================
    // 工具：错误响应
    // =============================================================
    private Map<String, Object> errorResponse(String code, String message, String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "error");
        response.put("code", code);
        response.put("message", message);
        if (name != null) {
            response.put("name", name);
        }
        return response;
    }

    // =============================================================
    // 字符串处理
    // =============================================================
    private String sanitizeNameFromPayload(Object value) {
        return sanitizeNameValue(value == null ? "" : value.toString());
    }

    private String sanitizeNameValue(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("[\\s\\p{Cntrl}]+", "-");
    }

    // =============================================================
    // 拓扑历史
    // =============================================================
    public List<Map<String, Object>> getTopologyHistory(String name, String campus) {
        List<TopologyDocumentsHis> histories;

        if (name != null && !name.trim().isEmpty()) {
            histories = topologyDocumentsHisMapper.selectList(
                    new QueryWrapper<TopologyDocumentsHis>()
                            .eq("name", name.trim())
                            .orderByDesc("saved_at")
            );
        } else if (campus != null && !campus.trim().isEmpty()) {
            histories = topologyDocumentsHisMapper.selectList(
                    new QueryWrapper<TopologyDocumentsHis>()
                            .eq("campus", campus.trim())
                            .orderByDesc("saved_at")
            );
        } else {
            histories = topologyDocumentsHisMapper.selectList(
                    new QueryWrapper<TopologyDocumentsHis>().orderByDesc("saved_at")
            );
        }

        return histories.stream().map(this::toTopologyHistoryMap).toList();
    }

    private Map<String, Object> toTopologyHistoryMap(TopologyDocumentsHis entity) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", entity.getId());
        result.put("name", entity.getName());
        result.put("description", entity.getDescription());
        result.put("campus", entity.getCampus());
        result.put("savedAt", entity.getSavedAt());
        try {
            result.put("diagram", objectMapper.readValue(entity.getDiagramJson(), Map.class));
        } catch (JsonProcessingException e) {
            result.put("diagram", Map.of());
        }
        return result;
    }

    // =============================================================
    // 重命名
    // =============================================================
    @Transactional
    public Map<String, Object> renameTopologyDocument(String oldName, String newName) {
        String resolvedFrom = sanitizeNameValue(oldName);
        String resolvedTo = sanitizeNameValue(newName);
        if (resolvedFrom.isBlank() || resolvedTo.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        TopologyDocuments entity = topologyDocumentsMapper.selectOne(
                new QueryWrapper<TopologyDocuments>().eq("name", resolvedFrom)
        );
        if (entity == null) {
            return Map.of("status", "error", "message", "Topology not found");
        }

        final Long entityId = entity.getId();
        TopologyDocuments sameName = topologyDocumentsMapper.selectOne(
                new QueryWrapper<TopologyDocuments>().eq("name", resolvedTo)
        );
        if (sameName != null && !Objects.equals(sameName.getId(), entityId)) {
            return Map.of("status", "error", "message", "Name already exists");
        }

        entity.setName(resolvedTo);
        entity.setUpdatedAt(LocalDateTime.now());
        topologyDocumentsMapper.updateById(entity);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("name", entity.getName());
        response.put("updatedAt", entity.getUpdatedAt());
        return response;
    }

    // =============================================================
    // 删除
    // =============================================================
    @Transactional
    public Map<String, Object> deleteTopologyDocument(String name) {
        String resolvedName = sanitizeNameValue(name);
        if (resolvedName.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        TopologyDocuments entity = topologyDocumentsMapper.selectOne(
                new QueryWrapper<TopologyDocuments>().eq("name", resolvedName)
        );
        if (entity == null) {
            return Map.of("status", "error", "message", "Topology not found");
        }

        topologyDocumentsMapper.deleteById(entity.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("deleted", true);
        response.put("name", resolvedName);
        return response;
    }

    // =============================================================
    // 当前图
    // =============================================================
    public Map<String, Object> getCurrentDiagram(String campus) {
        TopologyDocuments entity = findCurrentTopology(campus);

        if (entity == null || entity.getDiagramJson() == null || entity.getDiagramJson().isBlank()) {
            return emptyDiagram();
        }

        try {
            return objectMapper.readValue(entity.getDiagramJson(), Map.class);
        } catch (JsonProcessingException e) {
            return emptyDiagram();
        }
    }

    private TopologyDocuments findCurrentTopology(String campus) {
        TopologyDocuments entity = null;

        if (campus != null && !campus.trim().isEmpty()) {
            entity = topologyDocumentsMapper.selectOne(
                    new QueryWrapper<TopologyDocuments>()
                            .eq("campus", campus.trim())
                            .orderByDesc("updated_at")
                            .last("LIMIT 1")
            );
        }

        if (entity == null) {
            entity = topologyDocumentsMapper.selectOne(
                    new QueryWrapper<TopologyDocuments>().eq("name", "default")
            );
        }

        if (entity == null) {
            entity = topologyDocumentsMapper.selectOne(
                    new QueryWrapper<TopologyDocuments>()
                            .orderByDesc("updated_at")
                            .last("LIMIT 1")
            );
        }

        return entity;
    }

    // =============================================================
    // 实时读数
    // =============================================================

    // =============================================================
    // 实体转换
    // =============================================================
    private Map<String, Object> toTopologySummary(TopologyDocuments entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("name", entity.getName());
        map.put("title", entity.getName());
        map.put("description", entity.getDescription());
        map.put("campus", entity.getCampus());
        map.put("type", entity.getType());
        map.put("updatedAt", entity.getUpdatedAt());
        return map;
    }

    private Map<String, Object> toPackagedTopologiesMap(PackagedTopologies entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId());
        map.put("name", entity.getName());
        map.put("description", entity.getDescription());
        map.put("updatedAt", entity.getUpdatedAt());
        try {
            Map<String, Object> entry = objectMapper.readValue(entity.getEntryJson(), Map.class);
            map.put("entry", entry);
        } catch (JsonProcessingException e) {
            map.put("entry", Map.of());
        }
        return map;
    }

    private Map<String, Object> toComponentCatalogMap(ComponentCatalog entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("kind", entity.getKind());
        map.put("name", entity.getName());
        map.put("category", entity.getCategory());
        map.put("description", entity.getDescription());
        map.put("updatedAt", entity.getUpdatedAt());

        try {
            Map<String, Object> catalogJson = objectMapper.readValue(entity.getCatalogJson(), Map.class);
            map.put("catalogJson", catalogJson);
        } catch (JsonProcessingException e) {
            map.put("catalogJson", Map.of());
        }

        return map;
    }

    // =============================================================
    // 其他工具
    // =============================================================
    private String stringValue(Object value) {
        if (value == null) return null;
        return value.toString();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private Map<String, Object> emptyDiagram() {
        Map<String, Object> empty = new LinkedHashMap<>();
        empty.put("version", "1");
        empty.put("elements", new ArrayList<>());
        empty.put("wires", new ArrayList<>());
        return empty;
    }
}