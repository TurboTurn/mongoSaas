package com.company.app.service.impl;

import com.company.app.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoServiceImpl implements DemoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    @Transactional//注意开启副本集时才能使用事务，单机mongo是不支持的
    public <T> T insert(T obj) {
        return mongoTemplate.insert(obj);
    }

}
