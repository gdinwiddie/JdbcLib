package com.idiacomputing.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DerbyJdbcConnectionFactory implements JdbcConnectionFactory {
	private static final String DB_LOCATION = "DBforTesting";
	public String jdbcDriver = "org.apache.derby.jdbc.EmbeddedDriver";
	public String protocol = "jdbc:derby:";
	
	public Connection getConnection() throws SQLException {
    	System.setProperty("derby.system.home", DB_LOCATION);
    	
		loadDriver(jdbcDriver);

		Properties props = new Properties();
		props.put("user", "user1");
		props.put("password", "user1");
		String dbName = DB_LOCATION + "/derbyDB";
		return DriverManager.getConnection(protocol + dbName + ";create=true",
				props);
	}

	private void loadDriver(String jdbcDriver) throws SQLException {
		try {
            Class.forName(jdbcDriver).newInstance(); // load the driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver '" + jdbcDriver + "' not found.", e);
        } catch (InstantiationException e) {
            throw new SQLException("Driver '" + jdbcDriver + "' not instantiated.", e);
		} catch (IllegalAccessException e) {
            throw new SQLException("Driver '" + jdbcDriver + "' illegal access.", e);
		}
	}

}
