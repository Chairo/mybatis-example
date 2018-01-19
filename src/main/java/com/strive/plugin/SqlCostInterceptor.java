package com.strive.plugin;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 监控SQL及其执行时间
 * 这里为什么使用StatementHandler去拦截？根据名字来看ParameterHandler和ResultSetHandler，前者处理参数，后者处理结果是不可能使用的，
 * 剩下的就是Executor和StatementHandler了。
 * <p>
 * 拦截StatementHandler而不是用Executor的原因是：
 * 1、Executor的update与query方法可能用到MyBatis的一二级缓存从而导致统计的并不是真正的SQL执行时间
 * 2、StatementHandler的update与query方法无论如何都会统计到PreparedStatement的execute方法执行时间，尽管也有一定误差（误差主要来自
 * 会将处理结果的时间也算上），但是相差不大
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class SqlCostInterceptor implements Interceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlCostInterceptor.class);

    /**
     * 拦截器的核心代码，唯一要注意的一点就是无论如何最终一定要返回invocation.proceed()，保证拦截器的层层调用。
     *
     * @param invocation
     * @return
     * @throws Throwable
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        long startTime = System.currentTimeMillis();
        StatementHandler statementHandler = (StatementHandler) target;

        try {
            return invocation.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long sqlCost = endTime - startTime;

            BoundSql boundSql = statementHandler.getBoundSql();
            String sql = boundSql.getSql();
            Object parameterObject = boundSql.getParameterObject();
            List<ParameterMapping> parameterMappingList = boundSql.getParameterMappings();

            // 格式化Sql语句，去除换行符，替换参数
            sql = this.formatSql(sql, parameterObject, parameterMappingList);
            LOGGER.info("SQL：[" + sql + "] 执行耗时：[" + sqlCost + " ms]");
        }
    }

    /**
     * 这里是为目标接口生成代理，不需要自己去写生成代理的方法，MyBatis的Plugin类已经为我们提供了wrap方法（当然如果有自己的逻辑也可以
     * 在Plugin.wrap方法前后加入，但是最终一定要使用Plugin.wrap方法生成代理）
     *
     * @param target 这里的target一定是一个接口，因此可以放心使用JDK本身提供的Proxy类，这里相当于就是如果该接口满足方法签名那么就为之生成一个代理。
     * @return
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 可以将一些配置属性配置在<plugin></plugin>的子标签<property />中，所有的配置属性会在形参Properties中，setProperties方法可以
     * 拿到配置的属性进行需要的处理。
     *
     * @param properties
     */
    @Override
    public void setProperties(Properties properties) {

    }

    private String formatSql(String sql, Object parameterObject, List<ParameterMapping> parameterMappingList) {
        // 输入sql字符串空判断
        if (StringUtils.isBlank(sql)) {
            return "";
        }

        // 美化sql
        sql = this.beautifySql(sql);

        // 不传参数的场景，直接返回
        if (parameterObject == null || CollectionUtils.isEmpty(parameterMappingList)) {
            return sql;
        }

        // 定义一个没有替换过占位符的sql，用于出异常时返回
        String sqlWithoutReplacePlaceholder = sql;

        try {
            if (CollectionUtils.isNotEmpty(parameterMappingList)) {
                Class<?> parameterObjectClass = parameterObject.getClass();

                // 如果参数是StrictMap且Value类型为Collection，获取key="list"的属性，这里主要是为了处理<foreach>循环时传入List这种参数的占位符替换
                // 例如select * from xxx where id in <foreach collection="list">...</foreach>
                if (this.isStrictMap(parameterObjectClass)) {
                    DefaultSqlSession.StrictMap<Collection<?>> strictMap = (DefaultSqlSession.StrictMap<Collection<?>>) parameterObject;
                    if (this.isList(strictMap.get("list").getClass())) {
                        sql = this.handleListParameter(sql, strictMap.get("list"));
                    }
                } else if (this.isMap(parameterObjectClass)) {
                    // 如果参数是Map则直接强转，通过map.get(key)方法获取真正的属性值
                    // 这里主要是为了处理<insert>、<delete>、<update>、<select>时传入parameterType为map的场景
                    Map<?, ?> paramMap = (Map<?, ?>) parameterObject;
                    sql = this.handleMapParameter(sql, paramMap, parameterMappingList);
                } else {
                    // 通用场景，比如传的是一个自定义的对象或者八种基本数据类型之一或者String
                    sql = this.handleCommonParameter(sql, parameterMappingList, parameterObjectClass, parameterObject);
                }
            }
        } catch (Exception e) {
            // 占位符替换过程中出现异常，则返回没有替换过占位符但是格式美化过的sql，这样至少保证sql语句比BoundSql中的sql更好看
            return sqlWithoutReplacePlaceholder;
        }
        return sql;
    }

    /**
     * 美化Sql
     */
    private String beautifySql(String sql) {
        sql = sql.replace("\n", "").replace("\t", "").replace("  ", " ").replace("( ", "(").replace(" )", ")").replace(" ,", ",");
        return sql;
    }

    /**
     * 处理参数为List的场景
     */
    private String handleListParameter(String sql, Collection<?> col) {
        if (col != null && col.size() != 0) {
            for (Object obj : col) {
                String value = null;
                Class<?> objClass = obj.getClass();

                // 只处理基本数据类型、基本数据类型的包装类、String这三种
                // 如果是复合类型也是可以的，不过复杂点且这种场景较少，写代码的时候要判断一下要拿到的是复合类型中的哪个属性
                if (this.isPrimitiveOrPrimitiveWrapper(objClass)) {
                    value = obj.toString();
                } else if (objClass.isAssignableFrom(String.class)) {
                    value = "\"" + obj.toString() + "\"";
                }

                sql = sql.replaceFirst("\\?", value);
            }
        }
        return sql;
    }

    /**
     * 处理参数为Map的场景
     */
    private String handleMapParameter(String sql, Map<?, ?> paramMap, List<ParameterMapping> parameterMappingList) {
        for (ParameterMapping parameterMapping : parameterMappingList) {
            Object propertyName = parameterMapping.getProperty();
            Object propertyValue = paramMap.get(propertyName);
            if (propertyValue != null) {
                if (propertyValue.getClass().isAssignableFrom(String.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }

                sql = sql.replaceFirst("\\?", propertyValue.toString());
            }
        }
        return sql;
    }

    /**
     * 处理通用的场景
     */
    private String handleCommonParameter(String sql, List<ParameterMapping> parameterMappingList, Class<?> parameterObjectClass, Object parameterObject) throws Exception {
        for (ParameterMapping parameterMapping : parameterMappingList) {
            String propertyValue;
            // 基本数据类型或者基本数据类型的包装类，直接toString即可获取其真正的参数值，其余直接取paramterMapping中的property属性即可
            if (this.isPrimitiveOrPrimitiveWrapper(parameterObjectClass)) {
                propertyValue = parameterObject.toString();
            } else {
                String propertyName = parameterMapping.getProperty();

                Field field = parameterObjectClass.getDeclaredField(propertyName);
                // 要获取Field中的属性值，这里必须将私有属性的accessible设置为true
                field.setAccessible(true);
                propertyValue = String.valueOf(field.get(parameterObject));
                if (parameterMapping.getJavaType().isAssignableFrom(String.class)) {
                    propertyValue = "\"" + propertyValue + "\"";
                }
            }

            sql = sql.replaceFirst("\\?", propertyValue);
        }
        return sql;
    }

    /**
     * 是否基本数据类型或者基本数据类型的包装类
     */
    private boolean isPrimitiveOrPrimitiveWrapper(Class<?> parameterObjectClass) {
        return parameterObjectClass.isPrimitive() ||
                (parameterObjectClass.isAssignableFrom(Byte.class) || parameterObjectClass.isAssignableFrom(Short.class) ||
                        parameterObjectClass.isAssignableFrom(Integer.class) || parameterObjectClass.isAssignableFrom(Long.class) ||
                        parameterObjectClass.isAssignableFrom(Double.class) || parameterObjectClass.isAssignableFrom(Float.class) ||
                        parameterObjectClass.isAssignableFrom(Character.class) || parameterObjectClass.isAssignableFrom(Boolean.class));
    }

    /**
     * 是否DefaultSqlSession的内部类StrictMap
     */
    private boolean isStrictMap(Class<?> parameterObjectClass) {
        return parameterObjectClass.isAssignableFrom(DefaultSqlSession.StrictMap.class);
    }

    /**
     * 是否List的实现类
     */
    private boolean isList(Class<?> clazz) {
        Class<?>[] interfaceClasses = clazz.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            if (interfaceClass.isAssignableFrom(List.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否Map的实现类
     */
    private boolean isMap(Class<?> parameterObjectClass) {
        Class<?>[] interfaceClasses = parameterObjectClass.getInterfaces();
        for (Class<?> interfaceClass : interfaceClasses) {
            if (interfaceClass.isAssignableFrom(Map.class)) {
                return true;
            }
        }
        return false;
    }
}
