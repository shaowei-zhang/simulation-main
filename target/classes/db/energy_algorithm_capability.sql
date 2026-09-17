CREATE TABLE IF NOT EXISTS energy_algorithm_capability (
    id BIGSERIAL PRIMARY KEY,
    algorithm_id BIGINT NOT NULL,
    capability_code VARCHAR(100) NOT NULL,
    capability_name_zh VARCHAR(200) NOT NULL,
    capability_name_en VARCHAR(200),
    capability_type VARCHAR(100),
    description TEXT,
    calculation_method TEXT,
    topology_required BOOLEAN NOT NULL DEFAULT TRUE,
    topology_calculation BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_algorithm_capability_algorithm
        FOREIGN KEY (algorithm_id) REFERENCES energy_algorithm(id) ON DELETE CASCADE,
    CONSTRAINT uk_algorithm_capability_code
        UNIQUE (algorithm_id, capability_code)
);

CREATE INDEX IF NOT EXISTS idx_algorithm_energy_type
    ON energy_algorithm(energy_type_id);
CREATE INDEX IF NOT EXISTS idx_algorithm_function_module
    ON energy_algorithm(function_module_id);
CREATE INDEX IF NOT EXISTS idx_capability_algorithm
    ON energy_algorithm_capability(algorithm_id);
CREATE INDEX IF NOT EXISTS idx_capability_enabled
    ON energy_algorithm_capability(algorithm_id, enabled);

INSERT INTO energy_type (type_code, type_name_zh, type_name_en, enabled, sort_order)
SELECT 'ELECTRIC', '电能', 'Electric', TRUE, 1
 WHERE NOT EXISTS (
     SELECT 1 FROM energy_type WHERE type_code = 'ELECTRIC' OR type_name_zh = '电能'
 );
INSERT INTO energy_type (type_code, type_name_zh, type_name_en, enabled, sort_order)
SELECT 'THERMAL', '热能', 'Thermal', TRUE, 2
 WHERE NOT EXISTS (
     SELECT 1 FROM energy_type WHERE type_code IN ('THERMAL', 'HEAT') OR type_name_zh = '热能'
 );

INSERT INTO energy_function_module (module_code, module_name_zh, module_name_en, enabled, sort_order)
SELECT 'ISLAND_MONITORING', '孤岛监测', 'Island Monitoring', TRUE, 1
WHERE NOT EXISTS (
    SELECT 1 FROM energy_function_module
    WHERE module_code = 'ISLAND_MONITORING' OR module_name_zh = '孤岛监测'
);
INSERT INTO energy_function_module (module_code, module_name_zh, module_name_en, enabled, sort_order)
SELECT 'CALCULATION', '计算', 'Calculation', TRUE, 2
WHERE NOT EXISTS (
    SELECT 1 FROM energy_function_module
    WHERE module_code = 'CALCULATION' OR module_name_zh = '计算'
);
INSERT INTO energy_function_module (module_code, module_name_zh, module_name_en, enabled, sort_order)
SELECT 'SIMULATION', '仿真推演', 'Simulation', TRUE, 3
WHERE NOT EXISTS (
    SELECT 1 FROM energy_function_module
    WHERE module_code = 'SIMULATION' OR module_name_zh = '仿真推演'
);
INSERT INTO energy_function_module (module_code, module_name_zh, module_name_en, enabled, sort_order)
SELECT 'OPTIMIZATION', '优化分析', 'Optimization', TRUE, 4
WHERE NOT EXISTS (
    SELECT 1 FROM energy_function_module
    WHERE module_code = 'OPTIMIZATION' OR module_name_zh = '优化分析'
);

INSERT INTO energy_algorithm (
    algorithm_code, algorithm_name_en, algorithm_name_zh, function_module_id,
    energy_type_id, algorithm_type, description, algorithm_method,
    open_source_project, github_url, language, topology_required,
    topology_calculation, sort_order, enabled
)
SELECT 'PANDAPOWER', 'pandapower', 'pandapower 电力系统计算', fm.id, et.id,
       'calculation_framework', '电力系统计算框架',
       'Newton-Raphson / PYPOWER / PowerGridModel / lightsim2grid',
       'pandapower', 'https://github.com/e2nIEE/pandapower', 'Python',
       TRUE, TRUE, 1, TRUE
FROM energy_type et, energy_function_module fm
WHERE et.type_code = 'ELECTRIC' AND fm.module_code = 'CALCULATION'
  AND NOT EXISTS (SELECT 1 FROM energy_algorithm WHERE algorithm_code = 'PANDAPOWER');

INSERT INTO energy_algorithm (
    algorithm_code, algorithm_name_en, algorithm_name_zh, function_module_id,
    energy_type_id, algorithm_type, description, algorithm_method,
    open_source_project, github_url, language, topology_required,
    topology_calculation, sort_order, enabled
)
SELECT 'PANDAPIPES', 'pandapipes', 'pandapipes 热力/气体管网计算', fm.id, et.id,
       'calculation_framework', '热力与气体管网计算框架', 'Pipeflow Calculation',
       'pandapipes', 'https://github.com/e2nIEE/pandapipes', 'Python',
       TRUE, TRUE, 1, TRUE
FROM energy_type et, energy_function_module fm
WHERE et.type_code IN ('THERMAL', 'HEAT') AND fm.module_code = 'CALCULATION'
  AND NOT EXISTS (SELECT 1 FROM energy_algorithm WHERE algorithm_code = 'PANDAPIPES');

INSERT INTO energy_algorithm (
    algorithm_code, algorithm_name_en, algorithm_name_zh, function_module_id,
    energy_type_id, algorithm_type, description, language, topology_required,
    topology_calculation, sort_order, enabled
)
SELECT 'ELECTRIC_ISLAND_DETECTION', 'Electric Island Detection', '电网孤岛检测', fm.id, et.id,
       'island_detection', '电力网络孤岛检测', 'Python', TRUE, TRUE, 1, TRUE
FROM energy_type et, energy_function_module fm
WHERE et.type_code = 'ELECTRIC' AND fm.module_code = 'ISLAND_MONITORING'
  AND NOT EXISTS (SELECT 1 FROM energy_algorithm WHERE algorithm_code = 'ELECTRIC_ISLAND_DETECTION');

INSERT INTO energy_algorithm (
    algorithm_code, algorithm_name_en, algorithm_name_zh, function_module_id,
    energy_type_id, algorithm_type, description, language, topology_required,
    topology_calculation, sort_order, enabled
)
SELECT 'THERMAL_ISLAND_DETECTION', 'Thermal Island Detection', '热力管网孤岛检测', fm.id, et.id,
       'island_detection', '热力管网孤岛检测', 'Python', TRUE, TRUE, 1, TRUE
FROM energy_type et, energy_function_module fm
WHERE et.type_code IN ('THERMAL', 'HEAT') AND fm.module_code = 'ISLAND_MONITORING'
  AND NOT EXISTS (SELECT 1 FROM energy_algorithm WHERE algorithm_code = 'THERMAL_ISLAND_DETECTION');

INSERT INTO energy_algorithm_capability (
    algorithm_id, capability_code, capability_name_zh, capability_name_en,
    capability_type, calculation_method, topology_required, topology_calculation,
    sort_order, enabled
)
SELECT a.id, v.code, v.name_zh, v.name_en, v.type, v.method, TRUE, TRUE, v.sort_order, TRUE
FROM energy_algorithm a
CROSS JOIN (VALUES
    ('POWER_FLOW', '潮流计算', 'Power Flow', 'power_flow', 'Newton-Raphson / PYPOWER / PowerGridModel / lightsim2grid', 1),
    ('OPTIMAL_POWER_FLOW', '最优潮流', 'Optimal Power Flow', 'optimal_power_flow', NULL, 2),
    ('SHORT_CIRCUIT', '短路计算', 'Short Circuit Calculation', 'short_circuit', 'IEC 60909', 3),
    ('STATE_ESTIMATION', '状态估计', 'State Estimation', 'state_estimation', 'Weighted Least Squares', 4),
    ('TIME_SERIES', '时间序列分析', 'Time Series Analysis', 'time_series', NULL, 5),
    ('NETWORK_TOPOLOGY', '网络拓扑分析', 'Network Topology Analysis', 'network_topology', NULL, 6)
) AS v(code, name_zh, name_en, type, method, sort_order)
WHERE a.algorithm_code = 'PANDAPOWER'
  AND NOT EXISTS (
      SELECT 1 FROM energy_algorithm_capability c
      WHERE c.algorithm_id = a.id AND c.capability_code = v.code
  );

INSERT INTO energy_algorithm_capability (
    algorithm_id, capability_code, capability_name_zh, capability_name_en,
    capability_type, description, topology_required, topology_calculation,
    sort_order, enabled
)
SELECT a.id, 'ISLAND_DETECTION',
       CASE a.algorithm_code WHEN 'ELECTRIC_ISLAND_DETECTION' THEN '电网孤岛检测' ELSE '热力管网孤岛检测' END,
       CASE a.algorithm_code WHEN 'ELECTRIC_ISLAND_DETECTION' THEN 'Electric Island Detection' ELSE 'Thermal Island Detection' END,
       'island_detection', '检测当前能源网络中的孤岛', TRUE, TRUE, 1, TRUE
FROM energy_algorithm a
WHERE a.algorithm_code IN ('ELECTRIC_ISLAND_DETECTION', 'THERMAL_ISLAND_DETECTION')
  AND NOT EXISTS (
      SELECT 1 FROM energy_algorithm_capability c
      WHERE c.algorithm_id = a.id AND c.capability_code = 'ISLAND_DETECTION'
  );

INSERT INTO energy_algorithm_capability (
    algorithm_id, capability_code, capability_name_zh, capability_name_en,
    capability_type, topology_required, topology_calculation, sort_order, enabled
)
SELECT a.id, v.code, v.name_zh, v.name_en, v.type, TRUE, TRUE, v.sort_order, TRUE
FROM energy_algorithm a
CROSS JOIN (VALUES
    ('PIPE_PRESSURE', '管道压力计算', 'Pipe Pressure Calculation', 'pipe_pressure', 1),
    ('NODE_PRESSURE', '节点压力计算', 'Node Pressure Calculation', 'node_pressure', 2),
    ('PUMP_CALCULATION', '泵计算', 'Pump Calculation', 'pump_calculation', 3),
    ('VALVE_CALCULATION', '阀门计算', 'Valve Calculation', 'valve_calculation', 4),
    ('HEAT_SOURCE_CALCULATION', '热源计算', 'Heat Source Calculation', 'heat_source_calculation', 5),
    ('HEAT_SINK_CALCULATION', '热沉计算', 'Heat Sink Calculation', 'heat_sink_calculation', 6),
    ('HEAT_NETWORK_CALCULATION', '热网计算', 'Heat Network Calculation', 'heat_network_calculation', 7),
    ('GAS_NETWORK_CALCULATION', '气网计算', 'Gas Network Calculation', 'gas_network_calculation', 8),
    ('PIPEFLOW_CALCULATION', '管流计算', 'Pipeflow Calculation', 'pipeflow_calculation', 9)
) AS v(code, name_zh, name_en, type, sort_order)
WHERE a.algorithm_code = 'PANDAPIPES'
  AND NOT EXISTS (
      SELECT 1 FROM energy_algorithm_capability c
      WHERE c.algorithm_id = a.id AND c.capability_code = v.code
  );
