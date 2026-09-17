package com.example.topo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"com.example.topo.repository", "com.example.topo.mapper"})
public class TopoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TopoApplication.class, args);
    }
}
