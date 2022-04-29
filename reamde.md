### mongoDB saas多租户数据源解决方案
####实现功能
- 覆写SimpleMongoDbFactory中getMongoClient和doGetMongoDatabase方法，实现进行db操作时自动切换至租户库
- 新增租户库运行时动态识别
- 支持mongoDB事务，支持一台服务器上跨DataBase事务
- 同一集群上的DataBase可共用MongoClient连接池，减少每个租户一个连接池模式时的连接开销
####使用方法
- 新建库saas，执行saas.sql初始化数据，注意：租户库也需
- DataSourceContext一般放于过滤器中，set当前租户corpKey，在执行mongoDB数据操作时根据当前corpKey，动态切换DataBase