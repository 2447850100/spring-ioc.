package com.xiaohu.springioc.service.impl;

import com.xiaohu.springioc.bean.Employees;
import com.xiaohu.springioc.service.EmployeesService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Version 1.0
 * @Author huqiang
 * @Description EmployeesServiceImpl
 * @Date 2023/11/29 11:08
 **/
@Service("11")
public class EmployeesServiceImpl implements EmployeesService {

    static List<Employees> list = new ArrayList<>();
    static {
        list.add(new Employees(1, "张三1", 18, "user1",new Date())) ;
        list.add(new Employees(2, "张三2", 18, "user2",new Date())) ;
        list.add(new Employees(3, "张三3", 18, "user3",new Date())) ;
    }
    @Override
    public List<Employees> findAll() {
        return list;
    }

    @Override
    public Optional<Employees> selectById(Integer id) {
        return list.stream().filter(it -> it.getId().equals(id)).findFirst();
    }
}