package com.example.topo.domain;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一接口返回对象
 *
 * @param <T> 返回数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 请求是否成功
     */
    private boolean success;

    /**
     * 状态码
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;


    /**
     * 成功
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                200,
                "操作成功",
                data
        );
    }


    /**
     * 成功 + 自定义消息
     */
    public static <T> ApiResponse<T> success(
            String message,
            T data) {

        return new ApiResponse<>(
                true,
                200,
                message,
                data
        );
    }


    /**
     * 失败
     */
    public static <T> ApiResponse<T> error(
            int code,
            String message) {

        return new ApiResponse<>(
                false,
                101,
                message,
                null
        );
    }
}

