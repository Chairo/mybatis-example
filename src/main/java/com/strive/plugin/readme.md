MyBatis允许在某一点拦截已映射语句执行的调用。默认情况下，MyBatis允许使用插件来拦截的方法包括：
1、Executor（update、query、flushStatements、commint、rollback、getTransaction、close、isClosed）
2、ParameterHandler（getParameterObject、setParameters）
3、ResultSetHandler（handleResultSets、handleOutputParameters）
4、StatementHandler（prepare、parameterize、batch、update、query）

当我们使用mybatis自带的Plugin的wrap方法时需要保证拦截器上有注解@Intercepts与@Signature，这两个注解是必须的，
因为Plugin的wrap方法会取这两个注解里面的参数。
@Intercepts中可以定义多个@Signature，一个@Signature表示符合如下条件的方法才会被拦截：
1、接口必须是type定义的类型
2、方法名必须和method一致
3、方法形参的Class类型必须和args定义Class类型顺序一致