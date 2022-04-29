package com.company.app.tenant;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "corpInfo")
public class CorpInfo {

    @Id
    private Integer id;
    private String corpKey;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private Integer deleteFlag;

}
