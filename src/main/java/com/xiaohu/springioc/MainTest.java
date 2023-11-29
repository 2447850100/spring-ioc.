package com.xiaohu.springioc;

import com.xiaohu.springioc.controller.EmployeesController;
import org.springframework.container.ClassPathXmlApplicationContext;

/**
 * @Version 1.0
 * @Author huqiang
 * @Description MainTest
 * @Date 2023/11/29 11:44
 **/
public class MainTest {

    public static void main(String[] args) {
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        //通过类型获取
        EmployeesController employeesController = classPathXmlApplicationContext.getBean(EmployeesController.class);
        employeesController.findEmployees();

        System.out.println("======================================================");
        //通过名称获取
        EmployeesController bean = (EmployeesController)classPathXmlApplicationContext.getBean("oc");
        bean.findEmployees();

    }
}