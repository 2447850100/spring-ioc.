package com.xiaohu.springioc.controller;

import com.xiaohu.springioc.bean.Employees;
import com.xiaohu.springioc.service.EmployeesService;
import org.springframework.stereotype.Autowired;
import org.springframework.stereotype.Controller;


/**
 * @Version 1.0
 * @Author huqiang
 * @Description EmployeesController
 * @Date 2023/11/29 11:08
 **/
@Controller(value = "oc")
public class EmployeesController {

    @Autowired("22")
    private EmployeesService employeesService;

    public void findEmployees() {
        employeesService.findAll().forEach(System.out::println);
    }

    public void selectById(Integer id) {
        Employees employees = employeesService.selectById(id).orElseThrow(() -> new IllegalArgumentException("数据不存在: " + id));
        System.out.println("employees = " + employees);
    }
}