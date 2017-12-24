package com.strive.script;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @ClassName: RunSqlScript
 * @Description: 执行sql脚本
 * @author: tanggang@winshare-edu.com
 * @date: 2017年12月21日 10:19
 */
public class RunSqlScript {

    public static void main(String[] args) throws SQLException, IOException {
        String dir = System.getProperty("user.dir") + "/src/main/java/";
        String pathName = dir + "com/strive/script/CreateDB.sql";

        UnpooledDataSource ds = new UnpooledDataSource();
        // 从mysql-connector-java 6.0版本开始需要将driverClass配置为com.mysql.cj.jdbc.Driver
        ds.setDriver("com.mysql.cj.jdbc.Driver");
        // 从mysql-connector-java 6.0版本开始必须添加serverTimezone参数
        // 从mysql-connector-java 6.0版本开始可以省略Url中的localhost:3306，例如jdbc:mysql:///mybatis_test
        ds.setUrl("jdbc:mysql://127.0.0.1:3306/mybatis_test?useUnicode=true&serverTimezone=UTC&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&allowMultiQueries=true");
        ds.setUsername("root");
        ds.setPassword("123456");

        try (Connection connection = ds.getConnection();
             Reader reader = new InputStreamReader(new FileInputStream(pathName))) {

            ScriptRunner runner = new ScriptRunner(connection);
            runner.setAutoCommit(true);
            runner.setStopOnError(false);
            runner.setLogWriter(null);
            runner.setErrorLogWriter(null);
            runner.runScript(reader);
        }
    }
}
