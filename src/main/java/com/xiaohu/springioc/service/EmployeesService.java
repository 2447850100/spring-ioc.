package com.xiaohu.springioc.service;

import com.xiaohu.springioc.bean.Employees;

import java.util.List;
import java.util.Optional;

/**
 * @Version 1.0
 * @Author xiaohugg
 * @Description EmployeesService
 * @Date 2023/11/29 11:08
 **/
public interface EmployeesService {
    List<Employees> findAll();

    Optional<Employees> selectById(Integer id);
}
