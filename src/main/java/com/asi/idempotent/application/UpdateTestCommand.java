package com.asi.idempotent.application;

import lombok.Data;

import java.io.Serializable;

/**
 * @author asi
 * @date 2023/10/7 15:55
 */
@Data
public class UpdateTestCommand implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别
     */
    private Integer sex;
}
