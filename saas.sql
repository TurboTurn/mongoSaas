
// ----------------------------
// Collection structure for corpDbInfo
// ----------------------------
db.getCollection("corpDbInfo").drop();
db.createCollection("corpDbInfo");

// ----------------------------
// Documents of corpDbInfo
// ----------------------------
db.getCollection("corpDbInfo").insert([ {
    _id: 1,
    dbUsername: "mongouser",
    dbPassword: "password",
    dbUrl: "mongodb://mongouser:password@127.0.0.1:27017/saas",
    deleteFlag: 1,
    dbName: "saas",
    dbHost: "127.0.0.1",
    dbPort: "27017"
} ]);

// ----------------------------
// Collection structure for corpInfo
// ----------------------------
db.getCollection("corpInfo").drop();
db.createCollection("corpInfo");

// ----------------------------
// Documents of corpInfo
// ----------------------------
db.getCollection("corpInfo").insert([ {
    _id: 1,
    corpKey: "corpA",
    deleteFlag: 1,
    dbUser: "mongouser",
    dbPassword: "password",
    dbUrl: "mongodb://mongouser:password@127.0.0.1:27017/saas_corpA"
} ]);
db.getCollection("corpInfo").insert([ {
    _id: 2,
    corpKey: "corpB",
    deleteFlag: 1,
    dbUser: "mongouser",
    dbPassword: "password",
    dbUrl: "mongodb://mongouser:password@127.0.0.1:27017/saas_corpB"
} ]);
db.getCollection("corpInfo").insert([ {
    _id: 3,
    corpKey: "corpC",
    deleteFlag: 1,
    dbUser: "mongouser",
    dbPassword: "password",
    dbUrl: "mongodb://mongouser:password@127.0.0.1:27017/saas_corpC"
} ]);

