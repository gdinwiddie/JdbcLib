package com.idiacomputing.jdbc;

public class HsqldbJdbcReaderTest extends JdbcReaderTestDB {

	public HsqldbJdbcReaderTest() {
		setJdbcConnectionFactory( new HsqldbJdbcConnectionFactory() );
	}

}
