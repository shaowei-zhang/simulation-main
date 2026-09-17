package com.example.topo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.topo.data.ComponentCatalogSeed;
import com.example.topo.dto.TopologySeedRequest;
import com.example.topo.entity.*;
import com.example.topo.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TopologyService {

    private final DeviceRepository deviceRepository;
    private final PipeRepository pipeRepository;
    private final RealtimeReadingRepository realtimeReadingRepository;
    private final TopologyDocumentRepository topologyDocumentRepository;
    private final TopologyDocumentHisRepository topologyDocumentHisRepository;
    private final ComponentCatalogRepository componentCatalogRepository;
    private final PackagedTopologyRepository packagedTopologyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void bootstrapCatalogData() {
        var entries = ComponentCatalogSeed.defaultEntries();
        if (entries.isEmpty()) {
            return;
        }

        componentCatalogRepository.delete(new QueryWrapper<>());

        for (var seed : entries) {
            try {
                ComponentCatalogEntity entity = new ComponentCatalogEntity();
                entity.setKind(seed.kind());
                entity.setName(seed.name());
                entity.setCategory(seed.category());
                entity.setDescription(seed.description());
                entity.setCatalogJson(objectMapper.writeValueAsString(seed.catalogJson()));
                entity.setUpdatedAt(LocalDateTime.now());
                componentCatalogRepository.insert(entity);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Failed to bootstrap component catalog", e);
            }
        }
    }

    @Transactional
    public Map<String, Object> seedTopology(TopologySeedRequest request) {
        int deviceCount = 0;
        if (request.getDevices() != null) {
            for (var device : request.getDevices()) {
                DeviceEntity entity = deviceRepository.findByDeviceCode(device.getDeviceCode())
                        .orElse(new DeviceEntity());
                entity.setDeviceCode(device.getDeviceCode());
                entity.setName(device.getName());
                entity.setType(device.getType());
                entity.setCategory(device.getCategory());
                entity.setX(device.getX() == null ? null : BigDecimal.valueOf(device.getX()));
                entity.setY(device.getY() == null ? null : BigDecimal.valueOf(device.getY()));
                entity.setValue(device.getValue() == null ? null : BigDecimal.valueOf(device.getValue()));
                entity.setUnit(device.getUnit());
                entity.setConfigJson(device.getConfigJson());
                entity.setRemarks(device.getRemarks());
                if (entity.getId() == null) {
                    deviceRepository.insert(entity);
                } else {
                    deviceRepository.updateById(entity);
                }
                deviceCount++;
            }
        }

        int pipeCount = 0;
        if (request.getPipes() != null) {
            for (var pipe : request.getPipes()) {
                PipeEntity entity = pipeRepository.findByPipeCode(pipe.getPipeCode())
                        .orElse(new PipeEntity());
                entity.setPipeCode(pipe.getPipeCode());
                entity.setName(pipe.getName());
                entity.setType(pipe.getType());
                entity.setFromDevice(pipe.getFromDevice());
                entity.setToDevice(pipe.getToDevice());
                entity.setLengthKm(pipe.getLengthKm() == null ? null : BigDecimal.valueOf(pipe.getLengthKm()));
                entity.setDiameterM(pipe.getDiameterM() == null ? null : BigDecimal.valueOf(pipe.getDiameterM()));
                entity.setFlowRate(pipe.getFlowRate() == null ? null : BigDecimal.valueOf(pipe.getFlowRate()));
                entity.setPressureDrop(pipe.getPressureDrop() == null ? null : BigDecimal.valueOf(pipe.getPressureDrop()));
                entity.setInletTemp(pipe.getInletTemp() == null ? null : BigDecimal.valueOf(pipe.getInletTemp()));
                entity.setOutletTemp(pipe.getOutletTemp() == null ? null : BigDecimal.valueOf(pipe.getOutletTemp()));
                entity.setUnit(pipe.getUnit());
                entity.setConfigJson(pipe.getConfigJson());
                if (entity.getId() == null) {
                    pipeRepository.insert(entity);
                } else {
                    pipeRepository.updateById(entity);
                }
                pipeCount++;
            }
        }

        return Map.of(
                "title", request.getTitle(),
                "deviceCount", deviceCount,
                "pipeCount", pipeCount,
                "seededAt", LocalDateTime.now()
        );
    }



    public Map<String, Object> getRealtimeSnapshot(String campus) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> diagram = getCurrentDiagram(campus);
        TopologyDocumentEntity current = findCurrentTopology(campus);
        result.put("id", current.getId());
        result.put("diagram", diagram);
        result.put("topologyName", current == null ? "" : current.getName());
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    public List<Map<String, Object>> getTopologyCatalog(String campus) {
        List<TopologyDocumentEntity> entities;
        if (campus == null || campus.trim().isEmpty()) {
            entities = topologyDocumentRepository.findAllByOrderByNameAsc();
        } else {
            entities = topologyDocumentRepository.findAllByCampusOrderByNameAsc(campus.trim());
        }
        return entities.stream()
                .map(this::toTopologySummary)
                .toList();
    }

    public List<Map<String, Object>> getComponentCatalog() {
        return componentCatalogRepository.findAllByOrderByKindAsc().stream()
                .map(this::toComponentCatalogMap)
                .toList();
    }

    public List<Map<String, Object>> getPackagedTopologyCatalog() {
        return packagedTopologyRepository.findAllByOrderByNameAsc().stream()
                .map(this::toPackagedTopologyMap)
                .toList();
    }

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

        PackagedTopologyEntity entity = packagedTopologyRepository.findByName(name)
                .orElseGet(() -> {
                    PackagedTopologyEntity item = new PackagedTopologyEntity();
                    item.setName(name);
                    return item;
                });

        entity.setName(name);
        entity.setDescription(firstNonBlank(stringValue(payload.get("description")), entity.getDescription(), "Saved packaged topology"));
        entity.setEntryJson(entryJson);
        entity.setUpdatedAt(LocalDateTime.now());
        if (entity.getId() == null) {
            packagedTopologyRepository.insert(entity);
        } else {
            packagedTopologyRepository.updateById(entity);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put("name", entity.getName());
        result.put("updatedAt", entity.getUpdatedAt());
        return result;
    }

    public Map<String, Object> deletePackagedTopology(String name) {
        String resolvedName = sanitizeNameValue(name);
        if (resolvedName.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        PackagedTopologyEntity entity = packagedTopologyRepository.findByName(resolvedName).orElse(null);
        if (entity == null) {
            return Map.of("status", "error", "message", "Packaged topology not found");
        }

        packagedTopologyRepository.deleteById(entity.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("deleted", true);
        response.put("name", resolvedName);
        return response;
    }

    public Map<String, Object> getComponentByKind(String kind) {
        String resolved = kind == null ? "" : kind.trim();
        if (resolved.isEmpty()) {
            return Map.of("status", "error", "message", "kind is required");
        }

        return componentCatalogRepository.findByKindIgnoreCase(resolved)
                .map(this::toComponentCatalogMap)
                .orElse(Map.of("status", "not_found", "kind", resolved));
    }

    public Map<String, Object> getDiagramByName(Long id) {
        if (id==null) {
            return emptyDiagram();
        }

        TopologyDocumentEntity entity = topologyDocumentRepository.selectById(id);

        if (entity == null || entity.getDiagramJson() == null || entity.getDiagramJson().isBlank()) {
            return emptyDiagram();
        }

        try {
            return objectMapper.readValue(entity.getDiagramJson(), Map.class);
        } catch (JsonProcessingException e) {
            return emptyDiagram();
        }
    }


    @Transactional
    public TopologyDocumentEntity getTopoDetail(Long id) {
        return topologyDocumentRepository.selectById(id);
    }


    @Transactional
    public Map<String, Object> saveTopologyDocument(
            Map<String, Object> payload) {

        Map<String, Object> diagramPayload =
                payload == null ? Map.of() : payload;

        // =========================================================
        // 1. 获取 id
        //
        // id 为空  -> 新增
        // id 存在  -> 更新
        // =========================================================

        String id = sanitizeNameFromPayload(
                diagramPayload.get("id")
        );

        // =========================================================
        // 2. 获取 name
        // =========================================================

        String name = sanitizeNameFromPayload(
                diagramPayload.get("name")
        );

        if (name.isBlank()) {
            name = "default";
        }

        final String resolvedName = name;

        // =========================================================
        // 3. 获取 diagram
        // =========================================================

        Object rawDiagram =
                diagramPayload.getOrDefault(
                        "diagram",
                        diagramPayload
                );

        String diagramJson;

        try {

            diagramJson =
                    objectMapper.writeValueAsString(rawDiagram);

        } catch (JsonProcessingException e) {

            throw new IllegalArgumentException(
                    "Invalid diagram JSON payload",
                    e
            );
        }

        // =========================================================
        // 4. 获取 type
        // =========================================================

        String type = sanitizeNameFromPayload(
                diagramPayload.get("type")
        );

        if (type.isBlank()) {
            type = "default";
        }

        // =========================================================
        // 5. 获取 campus
        // =========================================================

        String campusValue =
                stringValue(
                        diagramPayload.get("campus")
                );

        // =========================================================
        // 6. 获取 description
        // =========================================================

        String description =
                firstNonBlank(
                        stringValue(
                                diagramPayload.get("description")
                        ),
                        stringValue(
                                diagramPayload.get("title")
                        ),
                        "Saved editor topology"
                );

        // =========================================================
        // 7. 新增 / 更新
        // =========================================================

        TopologyDocumentEntity entity;

        boolean created;

        // =========================================================
        //                    新增
        // =========================================================

        if (StringUtils.isEmpty(id)) {

            boolean nameExists =
                    topologyDocumentRepository
                            .findByName(resolvedName)
                            .isPresent();

            if (nameExists) {

                Map<String, Object> response =
                        new LinkedHashMap<>();

                response.put(
                        "status",
                        "error"
                );

                response.put(
                        "code",
                        "101"
                );

                response.put(
                        "message",
                        "拓扑名称已存在，请使用其他名称"
                );

                response.put(
                        "name",
                        resolvedName
                );

                return response;
            }


            // -----------------------------------------------------
            // 创建新实体
            // -----------------------------------------------------

            entity =
                    new TopologyDocumentEntity();

            entity.setName(resolvedName);

            entity.setDescription(
                    description
            );

            created = true;
        }

        // =========================================================
        //                    更新
        // =========================================================

        else {

            Long documentId;

            // -----------------------------------------------------
            // 解析 ID
            // -----------------------------------------------------

            try {

                documentId =
                        Long.valueOf(id);

            } catch (NumberFormatException e) {

                Map<String, Object> response =
                        new LinkedHashMap<>();

                response.put(
                        "status",
                        "error"
                );

                response.put(
                        "code",
                        "INVALID_ID"
                );

                response.put(
                        "message",
                        "无效的拓扑 ID：" + id
                );

                return response;
            }

            // -----------------------------------------------------
            // 根据 ID 查询
            // -----------------------------------------------------

            entity =
                    topologyDocumentRepository
                            .selectById(documentId);

            //增加更具name查询，查询数据库中是否已经存在name相同的


            if (entity == null) {

                Map<String, Object> response =
                        new LinkedHashMap<>();

                response.put(
                        "status",
                        "error"
                );

                response.put(
                        "code",
                        "TOPOLOGY_NOT_FOUND"
                );

                response.put(
                        "message",
                        "未找到对应的拓扑，ID：" + documentId
                );

                return response;
            }

            Optional<TopologyDocumentEntity> sameNameEntity =
                    topologyDocumentRepository.findByName(resolvedName);

            if (sameNameEntity.isPresent()
                    && !Objects.equals(sameNameEntity.get().getId(), documentId)) {

                Map<String, Object> response =
                        new LinkedHashMap<>();

                response.put(
                        "status",
                        "error"
                );

                response.put(
                        "code",
                        "101"
                );

                response.put(
                        "message",
                        "拓扑名称已存在，请使用其他名称"
                );

                response.put(
                        "name",
                        resolvedName
                );

                return response;
            }

            created = false;
        }

        // =========================================================
        // 8. 更新实体字段
        // =========================================================

        entity.setName(
                resolvedName
        );

        entity.setDiagramJson(
                diagramJson
        );

        entity.setDescription(
                description
        );

        // ---------------------------------------------------------
        // campus
        //
        // 如果请求里面有 campus，则更新
        // 如果没有，则保留原来的 campus
        // ---------------------------------------------------------

        if (campusValue != null
                && !campusValue.trim().isEmpty()) {

            entity.setCampus(
                    campusValue.trim()
            );
        }

        // ---------------------------------------------------------
        // type
        // ---------------------------------------------------------

        entity.setType(
                type
        );

        // ---------------------------------------------------------
        // 更新时间
        // ---------------------------------------------------------

        entity.setUpdatedAt(
                LocalDateTime.now()
        );

        // =========================================================
        // 9. 保存主表
        // =========================================================

        if (created) {
            topologyDocumentRepository.insert(entity);
        } else {
            topologyDocumentRepository.updateById(entity);
        }

        // =========================================================
        // 10. 保存历史表
        // =========================================================

        TopologyDocumentHisEntity history =
                new TopologyDocumentHisEntity();

        history.setName(
                entity.getName()
        );

        history.setDiagramJson(
                entity.getDiagramJson()
        );

        history.setDescription(
                entity.getDescription()
        );

        history.setCampus(
                entity.getCampus()
        );

        // 历史表保存 type
        history.setType(
                entity.getType()
        );

        history.setSavedAt(
                entity.getUpdatedAt()
        );

        topologyDocumentHisRepository.insert(history);

        // =========================================================
        // 11. 返回结果
        // =========================================================

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "status",
                "ok"
        );

        // true  = 新增
        // false = 更新
        response.put(
                "created",
                created
        );

        // 数据库 ID
        response.put(
                "id",
                entity.getId()
        );

        response.put(
                "name",
                entity.getName()
        );

        response.put(
                "type",
                entity.getType()
        );

        response.put(
                "campus",
                entity.getCampus()
        );

        response.put(
                "updatedAt",
                entity.getUpdatedAt()
        );

        return response;
    }


// =============================================================
// 工具方法
// =============================================================


    private String sanitizeNameFromPayload(Object value) {
        return sanitizeNameValue(
                value == null ? "" : value.toString()
        );
    }

    private String sanitizeNameValue(String value) {

        if (value == null) {
            return "";
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return "";
        }

        return trimmed.replaceAll(
                "[\\s\\p{Cntrl}]+",
                "-"
        );
    }


    public List<Map<String, Object>> getTopologyHistory(String name, String campus) {
        List<TopologyDocumentHisEntity> histories;
        if (name != null && !name.trim().isEmpty()) {
            histories = topologyDocumentHisRepository.findAllByNameOrderBySavedAtDesc(name.trim());
        } else if (campus != null && !campus.trim().isEmpty()) {
            histories = topologyDocumentHisRepository.findAllByCampusOrderBySavedAtDesc(campus.trim());
        } else {
            histories = topologyDocumentHisRepository.findAllByOrderBySavedAtDesc();
        }
        return histories.stream().map(this::toTopologyHistoryMap).toList();
    }

    private Map<String, Object> toTopologyHistoryMap(TopologyDocumentHisEntity entity) {
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

    @Transactional
    public Map<String, Object> renameTopologyDocument(String oldName, String newName) {
        String resolvedFrom = sanitizeNameValue(oldName);
        String resolvedTo = sanitizeNameValue(newName);
        if (resolvedFrom.isBlank() || resolvedTo.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        TopologyDocumentEntity entity = topologyDocumentRepository.findByName(resolvedFrom).orElse(null);
        if (entity == null) {
            return Map.of("status", "error", "message", "Topology not found");
        }

        final Long entityId = entity.getId();
        if (topologyDocumentRepository.findByName(resolvedTo)
                .filter(existing -> !Objects.equals(existing.getId(), entityId))
                .isPresent()) {
            return Map.of("status", "error", "message", "Name already exists");
        }

        entity.setName(resolvedTo);
        entity.setUpdatedAt(LocalDateTime.now());
        topologyDocumentRepository.updateById(entity);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("name", entity.getName());
        response.put("updatedAt", entity.getUpdatedAt());
        return response;
    }

    @Transactional
    public Map<String, Object> deleteTopologyDocument(String name) {
        String resolvedName = sanitizeNameValue(name);
        if (resolvedName.isBlank()) {
            return Map.of("status", "error", "message", "Name is required");
        }

        TopologyDocumentEntity entity = topologyDocumentRepository.findByName(resolvedName).orElse(null);
        if (entity == null) {
            return Map.of("status", "error", "message", "Topology not found");
        }

        topologyDocumentRepository.deleteById(entity.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("deleted", true);
        response.put("name", resolvedName);
        return response;
    }

    public Map<String, Object> getCurrentDiagram(String campus) {
        TopologyDocumentEntity entity = findCurrentTopology(campus);

        if (entity == null || entity.getDiagramJson() == null || entity.getDiagramJson().isBlank()) {
            return emptyDiagram();
        }

        try {
            return objectMapper.readValue(entity.getDiagramJson(), Map.class);
        } catch (JsonProcessingException e) {
            return emptyDiagram();
        }
    }

    private TopologyDocumentEntity findCurrentTopology(String campus) {
        TopologyDocumentEntity entity = null;
        if (campus != null && !campus.trim().isEmpty()) {
            entity = topologyDocumentRepository.findAllByCampusOrderByUpdatedAtDesc(campus.trim())
                    .stream().findFirst().orElse(null);
        }
        if (entity == null) {
            entity = topologyDocumentRepository.findByName("default").orElse(null);
        }
        if (entity == null) {
            entity = topologyDocumentRepository.findAllByOrderByUpdatedAtDesc().stream().findFirst().orElse(null);
        }
        return entity;
    }

    public void insertReading(String deviceCode, String metricName, BigDecimal value, String unit, String source) {
        RealtimeReadingEntity entity = new RealtimeReadingEntity();
        entity.setDeviceCode(deviceCode);
        entity.setMetricName(metricName);
        entity.setValue(value);
        entity.setUnit(unit);
        entity.setSource(source);
        entity.setCollectedAt(LocalDateTime.now());
        realtimeReadingRepository.insert(entity);
    }

    public List<Map<String, Object>> getLatestReadings() {
        return realtimeReadingRepository.findTop20ByOrderByCollectedAtDesc().stream()
                .map(this::toReadingMap)
                .toList();
    }

    private Map<String, Object> toReadingMap(RealtimeReadingEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("deviceCode", entity.getDeviceCode());
        map.put("metricName", entity.getMetricName());
        map.put("value", entity.getValue());
        map.put("unit", entity.getUnit());
        map.put("collectedAt", entity.getCollectedAt());
        map.put("source", entity.getSource());
        return map;
    }

    private Map<String, Object> toTopologySummary(TopologyDocumentEntity entity) {
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

    private Map<String, Object> toPackagedTopologyMap(PackagedTopologyEntity entity) {
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

    private Map<String, Object> toComponentCatalogMap(ComponentCatalogEntity entity) {
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
