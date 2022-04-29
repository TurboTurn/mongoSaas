package com.company.app;

import com.company.app.entity.Person;
import com.company.app.tenant.DataSourceContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MongoTemplateTest {

    @Autowired
    private MongoTemplate mongoTemplate;


    @Test
    public void t2() {
        DataSourceContext.setDateSourceKey("corpA");
        Person person = new Person("张三", 20, "深圳");
        mongoTemplate.insert(person);
    }
}
