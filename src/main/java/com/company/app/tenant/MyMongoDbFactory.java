package com.company.app.tenant;



import static com.company.app.tenant.TenantConfiguration.corpKeyDbNameMap;
import static com.company.app.tenant.TenantConfiguration.corpKeyMongoClientMap;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Slf4j
public class MyMongoDbFactory extends SimpleMongoDbFactory {

    @Autowired
    private TenantConfiguration tenantConfiguration;

    public MyMongoDbFactory(MongoClient mongoClient, String databaseName) {
        super(mongoClient, databaseName);
    }

    @Override
    protected void closeClient() {
        getMongoClient().close();
    }

    @Override
    protected MongoDatabase doGetMongoDatabase(String dbName) {
        String corpKey = DataSourceContext.getDateSourceKey();
        if (corpKey != null) {
            MongoClient mongoClient = getMongoClient();
            String databaseName = corpKeyDbNameMap.get(corpKey);
            log.info("切换到租户[{}]的MongoDatabase [{}]", corpKey, databaseName);
            return mongoClient.getDatabase(databaseName);
        } else {
            log.info("切换到[主库]MongoDatabase [{}]", dbName);
            return getMongoClient().getDatabase(dbName);
        }
    }



    @Override
    protected MongoClient getMongoClient() {
        String corpKey = DataSourceContext.getDateSourceKey();
        if (corpKey != null) {
            MongoClient mongoClient = corpKeyMongoClientMap.get(corpKey);
            if (mongoClient != null) {
                return mongoClient;
            } else {//重新加载mongoClient
                DataSourceContext.setDateSourceKey(null);
                //从主库查租户信息
                CorpInfo corpInfo = tenantConfiguration.mongoTemplate().findOne(
                        Query.query(Criteria.where("deleteFlag").is(1).and("corpKey").is(corpKey)),
                        CorpInfo.class);
                if (corpInfo == null) {//租户不存在
                    log.error("租户{}没有对应的mongoClient,并且数据库没有租户信息", corpKey);
                    throw new RuntimeException("租户" + corpKey + "没有绑定的mongoClient,并且数据库没有租户信息");
                } else {//租户存在，刷新数据源
                    log.info("租户{}没有对应的mongoClient,但数据库存在租户信息,将重新刷新数据源", corpKey);
                    tenantConfiguration.refresh();
                    DataSourceContext.setDateSourceKey(corpKey);//恢复线程变量
                    mongoClient = corpKeyMongoClientMap.get(corpKey);
                    if (mongoClient != null) {
                        return mongoClient;
                    } else {
                        log.error("刷新数据源后租户{}仍然没有对应的mongoClient", corpKey);
                        throw new RuntimeException("刷新数据源后租户" + corpKey + "仍然没有绑定的mongoClient");
                    }
                }
            }
        }
        return super.getMongoClient();//获取主库mongoClient
    }
}
