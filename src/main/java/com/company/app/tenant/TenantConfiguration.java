package com.company.app.tenant;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@Configuration
@Slf4j
public class TenantConfiguration implements ApplicationRunner {

    //每个租户对应的连接池
    public static Map<String, MongoClient> corpKeyMongoClientMap = new LinkedHashMap<>();
    //用于共用连接池，相同ip:port的租户库共用一个连接池
    public static Map<String, MongoClient> hostnameMongoClientMap = new LinkedHashMap<>();
    public static Map<String, String> corpKeyDbNameMap = new LinkedHashMap<>();

    //mongodb://username:password@hostname:port/dbname
    String patternStr = "mongodb://(.*)@(.*):(.*)/(.*)";
    Pattern pattern = Pattern.compile(patternStr);

    @Autowired
    private MongoProperties prop;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        refresh();
    }

    //刷新连接信息
    public synchronized void refresh() {
        //取租户信息
        List<CorpInfo> corpInfoList = mongoTemplate().find(Query.query(Criteria.where("deleteFlag").is(1)),
                CorpInfo.class);
        //取公共账号配置
        List<CorpDbInfo> corpDbInfoList = mongoTemplate().find(Query.query(Criteria.where("deleteFlag").is(1)),
                CorpDbInfo.class);
        Map<String, CorpDbInfo> corpDbInfoMap = corpDbInfoList.stream()
                .collect(Collectors.toMap(e -> e.getDbHost() + ":" + e.getDbPort(), e -> e));
        for (CorpInfo corpInfo : corpInfoList) {
            Matcher m = pattern.matcher(corpInfo.getDbUrl());
            if (m.find()) {
                String user = m.group(1);
                String ip = m.group(2);
                Integer port = Integer.valueOf(m.group(3));
                String hostName = ip + ":" + port;
                String dbName = m.group(4);
                MongoClient mongoClient;
                if (!hostnameMongoClientMap.containsKey(hostName)) {
                    if (corpDbInfoMap.containsKey(hostName)) {//配置了公共账号
                        CorpDbInfo corpDbInfo = corpDbInfoMap.get(hostName);
                        //使用公共账号创建连接池
                        mongoClient = getMongoClientByParam(corpDbInfo.getDbHost(), corpDbInfo.getDbPort(),
                                corpDbInfo.getDbUser(), corpDbInfo.getDbPassword(), corpDbInfo.getDbName());
                        hostnameMongoClientMap.put(hostName, mongoClient);//mongoClient缓存起来复用
                        log.info("创建租户库mongoClient, hostname:{}, database:{}", hostName, corpDbInfo.getDbName());
                    } else {
                        mongoClient = getMongoClientByParam(ip, port, corpInfo.getDbUser(),
                                corpInfo.getDbPassword(), dbName);
                        log.info("创建租户库mongoClient, hostname:{}, database:{}", hostName, dbName);
                    }
                } else {
                    mongoClient = hostnameMongoClientMap.get(hostName);
                }
                corpKeyMongoClientMap.put(corpInfo.getCorpKey(), mongoClient);
                corpKeyDbNameMap.put(corpInfo.getCorpKey(), dbName);//租户corpKey映射实际的dbName，可能出现两者不同的情况
                log.info("已加载租户[{}] Database - [{}]", corpInfo.getCorpKey(), dbName);
            } else {
                log.error("mongoUrl格式错误");
            }
        }//for
    }


    @Bean(name = "mongoTemplate")
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoDbFactory());
    }

    //主库工厂
    @Bean(name = "mongoDbFactory")
    public MongoDbFactory mongoDbFactory() {
        //创建认证客户端(存在用户名和密码)
        MongoClient mongoClient = getMongoClientByParam(prop.getHost(), prop.getPort(), prop.getUsername(),
                new String(prop.getPassword()), prop.getDatabase());
        log.info("创建主库mongoClient, hostname:{}:{}, database:{}", prop.getHost(), prop.getPort(), prop.getDatabase());
        hostnameMongoClientMap.put(prop.getHost() + ":" + prop.getPort(), mongoClient);//该账号需要授权服务器上其他租户库读写权限
        corpKeyMongoClientMap.put(null, mongoClient);//设置主库的mongoClient
        return new MyMongoDbFactory(mongoClient, prop.getDatabase());
    }

    @Bean("transactionManager")
    public MongoTransactionManager transactionManager(MongoDbFactory mongoDbFactory) {
        return new MongoTransactionManager(mongoDbFactory);
    }

    public static MongoClient getMongoClientByParam(String host, Integer port, String userName, String passWord,
            String dataBase) {
        MongoClientOptions.Builder builder = MongoClientOptions.builder();
        builder.connectTimeout(10000);
        builder.maxWaitTime(120000);
        builder.connectionsPerHost(500);
        builder.minConnectionsPerHost(3);
        //builder.requiredReplicaSetName();
        //builder.threadsAllowedToBlockForConnectionMultiplier();
        //builder.serverSelectionTimeout();
        //builder.maxConnectionIdleTime();
        //builder.maxConnectionLifeTime();
        //builder.socketTimeout());
        //builder.socketKeepAlive();
        //builder.sslEnabled());
        //builder.sslInvalidHostNameAllowed();
        //builder.alwaysUseMBeans();
        //builder.heartbeatFrequency();
        //builder.minHeartbeatFrequency();
        //builder.heartbeatConnectTimeout();
        //builder.heartbeatSocketTimeout();
        //builder.localThreshold();

        MongoClientOptions mongoClientOptions = builder.build();
        // MongoDB地址列表,如果有多个ip地址，那么配置文件里面可以用逗号分隔ip地址，这里再把每一个ip地址和端口号添加进list里面
        List<ServerAddress> serverAddresses = new ArrayList<>();
        ServerAddress serverAddress = new ServerAddress(host, port);
        serverAddresses.add(serverAddress);

        //连接认证
        MongoCredential mongoCredential = MongoCredential.createScramSha1Credential(userName, dataBase,
                passWord.toCharArray());

        return new MongoClient(serverAddresses, mongoCredential, mongoClientOptions);
    }

}
