package com.example.topo.controller;

import com.example.topo.dto.TopologySeedRequest;
import com.example.topo.domain.ApiResponse;
import com.example.topo.service.TopologyService;
import com.example.topo.service.TopologyAlgorithmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TopologyController {

    private final TopologyService topologyService;
    private final TopologyAlgorithmService topologyAlgorithmService;

    @PostMapping("/topology/seed")
    public ApiResponse<?> seedTopology(@Valid @RequestBody TopologySeedRequest request) {
        return ApiResponse.success(topologyService.seedTopology(request));
    }

    @GetMapping("/topology/current")
    public ApiResponse<?> getCurrentDiagram(@RequestParam(required = false) String campus) {
        return ApiResponse.success(topologyService.getCurrentDiagram(campus));
    }

    @GetMapping("/topology/catalog")
    public ApiResponse<?> getTopologyCatalog(@RequestParam(required = false) String campus) {
        return ApiResponse.success(topologyService.getTopologyCatalog(campus));
    }

    @GetMapping("/topology/document")
    public ApiResponse<?> getTopologyDocument(@RequestParam Long id) {
        return ApiResponse.success(topologyService.getDiagramByName(id));
    }
    @GetMapping("/topology/getTopoDetail")
    public ApiResponse<?> getTopoDetail(@RequestParam Long id) {
        return ApiResponse.success(topologyService.getTopoDetail(id));
    }

    @GetMapping("/topology/algorithms")
    public ApiResponse<?> getTopologyAlgorithmsByName(
            @RequestParam(required = false) String topologyName,
            @RequestParam(required = false) String name
    ) {
        return wrapServiceResult(topologyAlgorithmService.getAlgorithmsForTopology(
                topologyName != null ? topologyName : name));
    }

    @GetMapping("/topology/algorithms/{topologyId}")
    public ApiResponse<?> getTopologyAlgorithms(@PathVariable Long topologyId) {
        return wrapServiceResult(topologyAlgorithmService.getAlgorithmsForTopology(topologyId));
    }

    @GetMapping("/topology/realtime")
    public ApiResponse<?> getRealtimeSnapshot(@RequestParam(required = false) String campus) {
        return ApiResponse.success(topologyService.getRealtimeSnapshot(campus));
    }

    @GetMapping("/components/catalog")
    public ApiResponse<?> getComponentCatalog() {
        return ApiResponse.success(topologyService.getComponentCatalog());
    }

    @GetMapping("/modules/catalog")
    public ApiResponse<?> getPackagedTopologyCatalog() {
        return ApiResponse.success(topologyService.getPackagedTopologyCatalog());
    }

    @PostMapping("/modules/save")
    public ApiResponse<?> savePackagedTopology(@RequestBody Map<String, Object> payload) {
        return wrapServiceResult(topologyService.savePackagedTopology(payload));
    }

    @DeleteMapping("/modules/delete/{name}")
    public ApiResponse<?> deletePackagedTopology(@PathVariable String name) {
        return wrapServiceResult(topologyService.deletePackagedTopology(name));
    }

    @GetMapping("/components/{kind}")
    public ApiResponse<?> getComponentByKind(@PathVariable String kind) {
        return wrapServiceResult(topologyService.getComponentByKind(kind));
    }

    @PostMapping("/topology/save")
    public ApiResponse<?> saveCurrentDiagram(@RequestBody Map<String, Object> payload) {
        return wrapServiceResult(topologyService.saveTopologyDocument(payload));
    }

    @GetMapping("/topology/history")
    public ApiResponse<?> getTopologyHistory(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String campus
    ) {
        return ApiResponse.success(topologyService.getTopologyHistory(name, campus));
    }

    @PostMapping("/topology/rename")
    public ApiResponse<?> renameTopologyDocument(@RequestBody Map<String, String> payload) {
        String oldName = payload == null ? "" : payload.getOrDefault("oldName", "");
        String newName = payload == null ? "" : payload.getOrDefault("newName", "");
        return wrapServiceResult(topologyService.renameTopologyDocument(oldName, newName));
    }

    @DeleteMapping("/topology/delete/{name}")
    public ApiResponse<?> deleteTopologyDocument(@PathVariable String name) {
        return wrapServiceResult(topologyService.deleteTopologyDocument(name));
    }

    @GetMapping("/topology/readings")
    public ApiResponse<?> getLatestReadings() {
        return ApiResponse.success(topologyService.getLatestReadings());
    }

    @PostMapping("/topology/reading")
    public ApiResponse<?> ingestReading(
            @RequestParam String deviceCode,
            @RequestParam String metricName,
            @RequestParam BigDecimal value,
            @RequestParam(required = false) String unit,
            @RequestParam(required = false, defaultValue = "backend-seed") String source
    ) {
        topologyService.insertReading(deviceCode, metricName, value, unit, source);
        return ApiResponse.success(Map.of("deviceCode", deviceCode, "metricName", metricName));
    }

    @GetMapping("/health")
    public ApiResponse<?> health() {
        return ApiResponse.success(Map.of("service", "topo-backend"));
    }

    private ApiResponse<?> wrapServiceResult(Map<String, Object> result) {
        String status = result == null ? null : String.valueOf(result.get("status"));
        if ("error".equals(status) || "not_found".equals(status)) {
            String code = result.get("code") == null
                    ? ("not_found".equals(status) ? "NOT_FOUND" : "BUSINESS_ERROR")
                    : String.valueOf(result.get("code"));
            String message = result.get("message") == null
                    ? "请求处理失败"
                    : String.valueOf(result.get("message"));
            return ApiResponse.error(101, message);
        }

        Map<String, Object> data = new java.util.LinkedHashMap<>();
        if (result != null) {
            data.putAll(result);
            data.remove("status");
        }
        return ApiResponse.success(data);
    }
}
