package com.example.topo.dto;

import lombok.Data;

import java.util.List;

@Data
public class TopologySeedRequest {
    private String title;
    private List<DeviceSeedDto> devices;
    private List<PipeSeedDto> pipes;

    @Data
    public static class DeviceSeedDto {
        private String deviceCode;
        private String name;
        private String type;
        private String category;
        private Double x;
        private Double y;
        private Double value;
        private String unit;
        private String configJson;
        private String remarks;
    }

    @Data
    public static class PipeSeedDto {
        private String pipeCode;
        private String name;
        private String type;
        private String fromDevice;
        private String toDevice;
        private Double lengthKm;
        private Double diameterM;
        private Double flowRate;
        private Double pressureDrop;
        private Double inletTemp;
        private Double outletTemp;
        private String unit;
        private String configJson;
    }
}
