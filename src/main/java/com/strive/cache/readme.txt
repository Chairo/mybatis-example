1、mybatis中开启二级缓存方式：
①、在mybatis-config.xml文件中配置<setting name="cacheEnabled" value="true" />
②、在具体的mapper xml中使用<cache>或者<cache-ref>标签配置

2、mybatis中默认使用的本地缓存(一级缓存)的实现类为org.apache.ibatis.cache.impl.PerpetualCache

3、