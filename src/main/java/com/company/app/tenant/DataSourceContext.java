package com.company.app.tenant;

//存储租户信息
public class DataSourceContext {

    private static final ThreadLocal<String> dateSourceThreadLocal = new InheritableThreadLocal<>();

    public static String getDateSourceKey() {
        return dateSourceThreadLocal.get();
    }

    public static void setDateSourceKey(String tenantKey) {
        dateSourceThreadLocal.set(tenantKey);
    }

    public static void clearDataSourceKey() {
        dateSourceThreadLocal.remove();
    }

}
