package com.ibeetl.bbs;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.druid.pool.DruidDataSource;

@Configuration
public class DataSourceConfiguration {
	
	@Bean(name = "datasource")
	public DataSource druidDataSource() {
		DruidDataSource druidDataSource = new DruidDataSource();
		druidDataSource.setDriverClassName(Test.MysqlDBConfig.driver);
		druidDataSource.setUrl(Test.MysqlDBConfig.url);
		druidDataSource.setUsername(Test.MysqlDBConfig.userName);
		druidDataSource.setPassword(Test.MysqlDBConfig.password);
		druidDataSource.setValidationQuery("SELECT 1 FROM DUAL");
		druidDataSource.setInitialSize(5);
		druidDataSource.setMaxActive(10);
		
		// try {
		// druidDataSource.setFilters("stat, wall");
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		return druidDataSource;
	}
}
