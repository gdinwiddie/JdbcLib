package com.idiacomputing.jdbc;

public class DerbyJdbcWriterTest extends JdbcWriterTestDB {

	public DerbyJdbcWriterTest() {
		setJdbcConnectionFactory( new DerbyJdbcConnectionFactory() );
	}

}
