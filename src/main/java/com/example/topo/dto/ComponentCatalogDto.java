package com.example.topo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComponentCatalogDto {
    private String kind;
    private String name;
    private String category;
    private String description;
    private String catalogJson;
}
