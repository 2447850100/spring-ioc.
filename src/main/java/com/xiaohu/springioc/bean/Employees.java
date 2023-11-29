package com.xiaohu.springioc.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @Version 1.0
 * @Author xiaohugg
 * @Description Order
 * @Date 2023/11/29 11:04
 **/
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Employees {

    /**
     * id
     */
    private Integer id;

    /**
     * 名称
     */
    private String name;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 职位
     */
    private String position;

    /**
     * 入职时间
     */
    private Date hireTime;
}