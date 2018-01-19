1、mybatis中开启二级缓存方式：
①、在mybatis-config.xml文件中配置<setting name="cacheEnabled" value="true" />
②、在具体的mapper xml中使用<cache>或者<cache-ref>标签配置

2、mybatis中默认使用的二级缓存为org.apache.ibatis.cache.impl.PerpetualCache，它是基于HashMap来实现的

3、mybatis中默认使用的二级缓存淘汰策略为org.apache.ibatis.cache.decorators.LruCache，它是基于LinkedHashMap来实现的

4、