package com.example.topo.service;


import com.example.topo.domain.TopologyDocuments;

import java.util.List;
import java.util.Map;

public interface TopologyService {
    Map<String, Object> getRealtimeSnapshot(String campus);

    List<Map<String, Object>> getTopologyCatalog(String campus);

    List<Map<String, Object>> getComponentCatalog();

    List<Map<String, Object>> getPackagedTopologyCatalog();

    Map<String, Object> savePackagedTopology(Map<String, Object> payload);

    Map<String, Object> deletePackagedTopology(String name);

    Map<String, Object> getComponentByKind(String kind);

    Map<String, Object> getDiagramByName(Long id);

    TopologyDocuments getTopoDetail(Long id);

    Map<String, Object> saveTopologyDocument(Map<String, Object> payload);


    Map<String, Object> renameTopologyDocument(String oldName, String newName);

    Map<String, Object> deleteTopologyDocument(String name);

    Map<String, Object> getCurrentDiagram(String campus);


}