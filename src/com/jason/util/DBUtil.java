package com.jason.util;

import org.apache.commons.dbcp.BasicDataSource;


import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static DataSource dataSource;

    public static void initDataSource() {
        InputStream is = null;
        Properties properties = new Properties();
        String driverClassName = null;
        String url = null;
        String username = null;
        String password = null;
        int initialSize = 0;
        int minIdle = 0;
        int maxIdle = 0;
        int maxWait = 0;
        int maxActive = 0;
        try {
            is = ClassLoader.getSystemClassLoader().getResourceAsStream("dbcp.properties");
            properties.load(is);
            driverClassName = properties.getProperty("driverClassName");
            url = properties.getProperty("url");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            initialSize = Integer.parseInt((properties.getProperty("initialSize").trim()));
            minIdle = Integer.parseInt((properties.getProperty("minIdle")).trim());
            maxIdle = Integer.parseInt((properties.getProperty("maxIdle")).trim());
            maxWait = Integer.parseInt((properties.getProperty("maxWait")).trim());
            maxActive = Integer.parseInt((properties.getProperty("maxActive")).trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        BasicDataSource bds = new BasicDataSource();
        bds.setUrl(url);
        bds.setDriverClassName(driverClassName);
        bds.setUsername(username);
        bds.setPassword(password);
        bds.setInitialSize(initialSize);
        bds.setMaxActive(maxActive);
        bds.setMinIdle(minIdle);
        bds.setMaxIdle(maxIdle);
        bds.setMaxWait(maxWait);
        dataSource = bds;
    }

    public static Connection getConnection() {
        Connection conn = null;
        try {
            if (dataSource == null) {
                initDataSource();
            }
            if (dataSource != null) {
                conn = dataSource.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public static DataSource getDataSource() {
        initDataSource();
        return dataSource;
    }
}
