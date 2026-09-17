//package com.example.topo.data;
//
//import com.example.topo.dto.TopologySeedRequest;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public final class FixedTopologySeed {
//    private FixedTopologySeed() {
//    }
//
//    public static String buildDiagramJson() {
//        return """
//                {
//                  "version": "1",
//                  "meta": {
//                    "title": "热能拓扑",
//                    "description": "数据库种子数据，页面首次加载时直接展示"
//                  },
//                  "elements": [
//                    {
//                      "id": "gas1",
//                      "kind": "gas",
//                      "name": "气源1",
//                      "params": {
//                        "p_nom": 12,
//                        "T": 23,
//                        "m_flow": 4
//                      }
//                    },
//                    {
//                      "id": "mgt1",
//                      "kind": "mgt",
//                      "name": "热回收机组",
//                      "params": {
//                        "P_el": 60,
//                        "eta_el": 29,
//                        "T_exh": 550
//                      }
//                    },
//                    {
//                      "id": "LiBr1",
//                      "kind": "LiBr",
//                      "name": "LiBr制冷机",
//                      "params": {
//                        "Q_cool": 350,
//                        "COP": 0.75,
//                        "T_drive": 88
//                      }
//                    }
//                  ],
//                  "layout": {
//                    "gas1": { "at": [-4857, -1908] },
//                    "mgt1": { "at": [-4621, -1957] },
//                    "LiBr1": { "at": [-4161, -1700] }
//                  },
//                  "wires": [
//                    {
//                      "id": "W1",
//                      "ends": ["gas1.t1", "mgt1.t1"],
//                      "kind": "pipe",
//                      "pipe_params": {
//                        "length_km": 0.05,
//                        "diameter_m": 0.15,
//                        "flow_rate": 4.0,
//                        "pressure_drop": 0.12
//                      }
//                    },
//                    {
//                      "id": "W2",
//                      "ends": ["mgt1.t1", "LiBr1.t1"],
//                      "kind": "pipe",
//                      "pipe_params": {
//                        "length_km": 0.05,
//                        "diameter_m": 0.15,
//                        "flow_rate": 3.8,
//                        "pressure_drop": 0.11
//                      }
//                    }
//                  ]
//                }
//                """;
//    }
//
//    public static TopologySeedRequest build() {
//        TopologySeedRequest request = new TopologySeedRequest();
//        request.setTitle("热能拓扑固定数据");
//
//        List<TopologySeedRequest.DeviceSeedDto> devices = new ArrayList<>();
//        devices.add(device("gas1", "气源1", "gas", "source", -4857.0, -1908.0, 12.0, "bar", "{\"p_nom\":12,\"T\":23,\"m_flow\":4}", "主气源"));
//        devices.add(device("mgt1", "热回收机组", "mgt", "generation", -4621.0, -1957.0, 60.0, "kW", "{\"P_el\":60,\"eta_el\":29,\"T_exh\":550}", "机组"));
//        devices.add(device("fghe1", "燃气热交换器", "fghe", "heat-exchanger", -4560.0, -1785.0, 90.0, "C", "{\"T_gas_in\":550,\"T_water_out\":90,\"eta_heat\":85}", "换热器"));
//        devices.add(device("electric-heater1", "电加热器", "electric-heater", "heater", -4599.0, -1663.0, 30.0, "kW", "{\"P\":30,\"T_set\":60}", "电加热"));
//        devices.add(device("cpmu1", "补水泵1", "cpmu", "pump", -4309.0, -1796.0, 6.0, "bar", "{\"p_set\":6,\"m_makeup\":2}", "补水"));
//        devices.add(device("LiBr1", "LiBr制冷机", "LiBr", "cooling", -4161.0, -1700.0, 350.0, "kW", "{\"Q_cool\":350,\"COP\":0.75,\"T_drive\":88}", "制冷主机"));
//        devices.add(device("CV1", "截止阀1", "CV", "valve", -4356.0, -1720.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("CV2", "截止阀2", "CV", "valve", -4366.0, -1660.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("BDV 1", "隔离阀1", "BDV ", "valve", -4286.0, -1660.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("BDV 2", "隔离阀2", "BDV ", "valve", -4646.0, -1770.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("BDV 3", "隔离阀3", "BDV ", "valve", -4666.0, -1720.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("phe1", "板式换热器", "phe", "heat-exchanger", -4290.0, -1558.0, 20.0, "m2", "{\"A\":20,\"U\":4000}", "换热"));
//        devices.add(device("BDV 4", "隔离阀4", "BDV ", "valve", -4316.0, -1600.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("cwst1", "冷水储罐", "cwst", "storage", -4440.0, -1425.0, 15.0, "m3", "{\"V\":15,\"SOC\":60,\"T_min\":5}", "冷水储能"));
//        devices.add(device("hwst1", "热水储罐", "hwst", "storage", -4319.0, -1424.0, 10.0, "m3", "{\"V\":10,\"SOC\":75,\"T_max\":95}", "热水储能"));
//        devices.add(device("BDV 7", "隔离阀7", "BDV ", "valve", -4300.0, -1300.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("BDV 8", "隔离阀8", "BDV ", "valve", -4200.0, -1300.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("fcu1", "风机盘管", "fcu", "terminal", -4050.0, -1500.0, 15.0, "kW", "{\"Q_cool\":15,\"Q_heat\":12,\"m_air\":5}", "末端"));
//        devices.add(device("achp1", "空气源热泵", "achp", "heat-pump", -3920.0, -1400.0, 12.0, "kW", "{\"Q_cool\":12,\"Q_heat\":12,\"COP\":1}", "热泵"));
//        devices.add(device("CV3", "截止阀3", "CV", "valve", -3700.0, -1540.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("BDV 5", "隔离阀5", "BDV ", "valve", -3490.0, -1400.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("BDV 6", "隔离阀6", "BDV ", "valve", -3300.0, -1400.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("CT1", "冷却塔", "ct", "cooling-tower", -3260.0, -1280.0, 8.0, "C", "{\"T_approach\":5,\"m_water\":2}", "冷却塔"));
//        devices.add(device("CV4", "截止阀4", "CV", "valve", -3150.0, -1160.0, 1.0, "state", "{}", "常开"));
//        devices.add(device("cpmu2", "补水泵2", "cpmu", "pump", -2960.0, -1040.0, 6.0, "bar", "{\"p_set\":6,\"m_makeup\":2}", "补水"));
//        devices.add(device("CV5", "截止阀5", "CV", "valve", -2750.0, -930.0, 1.0, "state", "{}", "常开"));
//        request.setDevices(devices);
//
//        List<TopologySeedRequest.PipeSeedDto> pipes = new ArrayList<>();
//        pipes.add(pipe("P1", "供气管1", "pipe", "gas1", "mgt1", 0.05, 0.15, 4.0, 0.12, 23.0, 20.0, "m/s", "{}"));
//        pipes.add(pipe("P2", "供气管2", "pipe", "mgt1", "fghe1", 0.05, 0.15, 3.8, 0.11, 20.0, 18.0, "m/s", "{}"));
//        pipes.add(pipe("P3", "循环管1", "pipe", "fghe1", "electric-heater1", 0.04, 0.12, 2.2, 0.08, 90.0, 87.0, "m/s", "{}"));
//        pipes.add(pipe("P4", "循环管2", "pipe", "electric-heater1", "cpmu1", 0.04, 0.12, 2.1, 0.07, 87.0, 85.0, "m/s", "{}"));
//        pipes.add(pipe("P5", "循环管3", "pipe", "cpmu1", "LiBr1", 0.05, 0.14, 2.0, 0.06, 85.0, 82.0, "m/s", "{}"));
//        pipes.add(pipe("P6", "供水管1", "pipe", "LiBr1", "cwst1", 0.06, 0.16, 1.5, 0.04, 82.0, 78.0, "m/s", "{}"));
//        pipes.add(pipe("P7", "供水管2", "pipe", "cwst1", "hwst1", 0.05, 0.14, 1.6, 0.05, 78.0, 75.0, "m/s", "{}"));
//        pipes.add(pipe("P8", "换热管1", "pipe", "hwst1", "fcu1", 0.04, 0.12, 1.4, 0.03, 75.0, 72.0, "m/s", "{}"));
//        pipes.add(pipe("P9", "换热管2", "pipe", "fcu1", "achp1", 0.04, 0.12, 1.3, 0.03, 72.0, 68.0, "m/s", "{}"));
//        pipes.add(pipe("P10", "冷却管1", "pipe", "achp1", "CV3", 0.05, 0.14, 1.4, 0.04, 68.0, 65.0, "m/s", "{}"));
//        pipes.add(pipe("P11", "冷却管2", "pipe", "CV3", "BDV 5", 0.05, 0.14, 1.4, 0.04, 65.0, 62.0, "m/s", "{}"));
//        pipes.add(pipe("P12", "冷却管3", "pipe", "BDV 5", "CT1", 0.06, 0.16, 1.3, 0.05, 62.0, 60.0, "m/s", "{}"));
//        request.setPipes(pipes);
//
//        return request;
//    }
//
//    private static TopologySeedRequest.DeviceSeedDto device(
//            String deviceCode,
//            String name,
//            String type,
//            String category,
//            Double x,
//            Double y,
//            Double value,
//            String unit,
//            String configJson,
//            String remarks
//    ) {
//        TopologySeedRequest.DeviceSeedDto dto = new TopologySeedRequest.DeviceSeedDto();
//        dto.setDeviceCode(deviceCode);
//        dto.setName(name);
//        dto.setType(type);
//        dto.setCategory(category);
//        dto.setX(x);
//        dto.setY(y);
//        dto.setValue(value);
//        dto.setUnit(unit);
//        dto.setConfigJson(configJson);
//        dto.setRemarks(remarks);
//        return dto;
//    }
//
//    private static TopologySeedRequest.PipeSeedDto pipe(
//            String pipeCode,
//            String name,
//            String type,
//            String fromDevice,
//            String toDevice,
//            Double lengthKm,
//            Double diameterM,
//            Double flowRate,
//            Double pressureDrop,
//            Double inletTemp,
//            Double outletTemp,
//            String unit,
//            String configJson
//    ) {
//        TopologySeedRequest.PipeSeedDto dto = new TopologySeedRequest.PipeSeedDto();
//        dto.setPipeCode(pipeCode);
//        dto.setName(name);
//        dto.setType(type);
//        dto.setFromDevice(fromDevice);
//        dto.setToDevice(toDevice);
//        dto.setLengthKm(lengthKm);
//        dto.setDiameterM(diameterM);
//        dto.setFlowRate(flowRate);
//        dto.setPressureDrop(pressureDrop);
//        dto.setInletTemp(inletTemp);
//        dto.setOutletTemp(outletTemp);
//        dto.setUnit(unit);
//        dto.setConfigJson(configJson);
//        return dto;
//    }
//}
