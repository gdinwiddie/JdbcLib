package com.idiacomputing.jdbc;

public class HsqldbJdbcWriterTest extends JdbcWriterTestDB {

	public HsqldbJdbcWriterTest() {
		setJdbcConnectionFactory( new HsqldbJdbcConnectionFactory() );
	}

}
