package com.example.topo.controller;

import com.example.topo.domain.result.ApiResponse;
import com.example.topo.service.TopologyService;
import com.example.topo.service.impl.TopologyAlgorithmServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 拓扑管理 REST 控制器
 * <p>
 * 提供拓扑文档的 CRUD、算法目录查询、组件目录、封装模块管理
 * 以及实时数据读取等统一接口入口。
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TopologyController {

    private final TopologyService topologyService;
    private final TopologyAlgorithmServiceImpl topologyAlgorithmServiceImpl;
//
//    /** 初始化/回填测试用种子数据（设备、管道） */
//    @PostMapping("/topology/seed")
//    public ApiResponse<?> seedTopology(@Valid @RequestBody TopologySeedRequest request) {
//        return ApiResponse.success(topologyService.seedTopology(request));
//    }

//    /** 获取当前校区（或全局默认）正在使用的拓扑图纸 */
//    @GetMapping("/topology/current")
//    public ApiResponse<?> getCurrentDiagram(@RequestParam(required = false) String campus) {
//        return ApiResponse.success(topologyService.getCurrentDiagram(campus));
//    }

    /**
     * 获取拓扑文档目录列表，可按校区过滤
     */
    @GetMapping("/topology/catalog")
    public ApiResponse<?> getTopologyCatalog(@RequestParam(required = false) String campus) {
        return ApiResponse.success(topologyService.getTopologyCatalog(campus));
    }

    /**
     * 根据文档 id 获取完整拓扑图纸 JSON
     * 前端点击目录中某条目时调用此接口加载图纸数据
     */
    @GetMapping("/topology/document")
    public ApiResponse<?> getTopologyDocument(@RequestParam Long id) {
        return ApiResponse.success(topologyService.getDiagramByName(id));
    }

    /**
     * 获取拓扑文档完整元数据（含 id、name、type、campus 等）
     */
    @GetMapping("/topology/getTopoDetail")
    public ApiResponse<?> getTopoDetail(@RequestParam Long id) {
        return ApiResponse.success(topologyService.getTopoDetail(id));
    }

    /**
     * 按拓扑名称查询该拓扑关联的算法能力目录（兼容旧参数名 name/topologyName）
     */
//    @GetMapping("/topology/algorithms")
//    public ApiResponse<?> getTopologyAlgorithmsByName(
//            @RequestParam(required = false) String topologyName,
//            @RequestParam(required = false) String name
//    ) {
//        return wrapServiceResult(topologyAlgorithmService.getAlgorithmsForTopology(
//                topologyName != null ? topologyName : name));
//    }

    /**
     * 按拓扑 id 查询该拓扑关联的算法能力目录（推荐方式）
     */
    @GetMapping("/topology/algorithms/{topologyId}")
    public ApiResponse<?> getTopologyAlgorithms(@PathVariable Long topologyId) {
        return ApiResponse.success(topologyAlgorithmServiceImpl.getAlgorithmsForTopology(topologyId));
    }

    /**
     * 展示默认画布，刚一加载进来显示的topo（默认topo）
     */
    @GetMapping("/topology/realtime")
    public ApiResponse<?> getRealtimeSnapshot(@RequestParam(required = false) String campus) {
        return ApiResponse.success(topologyService.getRealtimeSnapshot(campus));
    }

    /**
     * 获取组件目录（电气/热力设备分类库）
     */
    @GetMapping("/components/catalog")
    public ApiResponse<?> getComponentCatalog() {
        return ApiResponse.success(topologyService.getComponentCatalog());
    }

    /**
     * 获取封装拓扑模块目录
     */
    @GetMapping("/modules/catalog")
    public ApiResponse<?> getPackagedTopologyCatalog() {
        return ApiResponse.success(topologyService.getPackagedTopologyCatalog());
    }

    /**
     * 保存/更新封装拓扑模块
     */
    @PostMapping("/modules/save")
    public ApiResponse<?> savePackagedTopology(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(topologyService.savePackagedTopology(payload));
    }

    /**
     * 删除封装拓扑模块
     */
    @DeleteMapping("/modules/delete/{name}")
    public ApiResponse<?> deletePackagedTopology(@PathVariable String name) {
        return ApiResponse.success(topologyService.deletePackagedTopology(name));
    }

    /**
     * 按组件类型获取对应目录数据（如 electric、thermal）
     */
    @GetMapping("/components/{kind}")
    public ApiResponse<?> getComponentByKind(@PathVariable String kind) {
        return ApiResponse.success(topologyService.getComponentByKind(kind));
    }

    /**
     * 保存/新建拓扑文档（含图纸 JSON、类型、校区等信息）
     */
    @PostMapping("/topology/save")
    public ApiResponse<?> saveCurrentDiagram(@RequestBody Map<String, Object> payload) {
        return ApiResponse.success(topologyService.saveTopologyDocument(payload));
    }

//    /** 查询拓扑文档的历史版本记录，可按名称或校区过滤 */
//    @GetMapping("/topology/history")
//    public ApiResponse<?> getTopologyHistory(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false) String campus
//    ) {
//        return ApiResponse.success(topologyService.getTopologyHistory(name, campus));
//    }

    /**
     * 重命名拓扑文档
     */
    @PostMapping("/topology/rename")
    public ApiResponse<?> renameTopologyDocument(@RequestBody Map<String, String> payload) {
        String oldName = payload == null ? "" : payload.getOrDefault("oldName", "");
        String newName = payload == null ? "" : payload.getOrDefault("newName", "");
        return ApiResponse.success(topologyService.renameTopologyDocument(oldName, newName));
    }

    /**
     * 删除拓扑文档
     */
    @DeleteMapping("/topology/delete/{name}")
    public ApiResponse<?> deleteTopologyDocument(@PathVariable String name) {
        return ApiResponse.success(topologyService.deleteTopologyDocument(name));
    }

//    /** 获取最近 20 条实时测量读数 */
//    @GetMapping("/topology/readings")
//    public ApiResponse<?> getLatestReadings() {
//        return ApiResponse.success(topologyService.getLatestReadings());
//    }

//    /** 录入一条实时测量数据（设备编码 + 指标名 + 值） */
//    @PostMapping("/topology/reading")
//    public ApiResponse<?> ingestReading(
//            @RequestParam String deviceCode,
//            @RequestParam String metricName,
//            @RequestParam BigDecimal value,
//            @RequestParam(required = false) String unit,
//            @RequestParam(required = false, defaultValue = "backend-seed") String source
//    ) {
//        topologyService.insertReading(deviceCode, metricName, value, unit, source);
//        return ApiResponse.success(Map.of("deviceCode", deviceCode, "metricName", metricName));
//    }

//    /** 健康检查接口 */
//    @GetMapping("/health")
//    public ApiResponse<?> health() {
//        return ApiResponse.success(Map.of("service", "topo-backend"));
//    }

    /**
     * 统一包装 Service 层返回结果
     * <p>
     * Service 层约定：返回包含 status 字段的 Map。
     * - status=error / not_found → 转为 ApiResponse.error(101, message)
     * - 其他 → 移除 status 字段，包装为 ApiResponse.success(data)
     */
//    private ApiResponse<?> wrapServiceResult(Map<String, Object> result) {
//        String status = result == null ? null : String.valueOf(result.get("status"));
//        if ("error".equals(status) || "not_found".equals(status)) {
//            String code = result.get("code") == null
//                    ? ("not_found".equals(status) ? "NOT_FOUND" : "BUSINESS_ERROR")
//                    : String.valueOf(result.get("code"));
//            String message = result.get("message") == null
//                    ? "请求处理失败"
//                    : String.valueOf(result.get("message"));
//            return ApiResponse.error(101, message);
//        }
//
//        Map<String, Object> data = new java.util.LinkedHashMap<>();
//        if (result != null) {
//            data.putAll(result);
//            data.remove("status");
//        }
//        return ApiResponse.success(data);
//    }
}
