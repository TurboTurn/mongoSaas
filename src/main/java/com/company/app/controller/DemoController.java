package com.company.app.controller;

import com.company.app.entity.Person;
import com.company.app.service.DemoService;
import com.company.app.tenant.DataSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/demo")
@RestController
public class DemoController {

    @Autowired
    private DemoService demoService;


    @GetMapping("/addPerson")
    public Person t1() {
        DataSourceContext.setDateSourceKey("corpA");
        Person person = new Person("张三", 20, "深圳");
        return demoService.insert(person);
    }

}
