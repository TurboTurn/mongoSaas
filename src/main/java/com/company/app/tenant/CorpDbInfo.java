package com.company.app.tenant;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 公共账号，同一台服务器的租户库共用一个连接池
 */
@Data
@Document(collection = "corpDbInfo")
public class CorpDbInfo {

    @Id
    private Integer id;
    private String dbHost;
    private Integer dbPort;
    private String dbName;
    private String dbUser;
    private String dbPassword;
    private String dbUrl;
    private Integer deleteFlag;

}
