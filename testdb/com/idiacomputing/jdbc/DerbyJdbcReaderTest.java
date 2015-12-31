package com.idiacomputing.jdbc;

public class DerbyJdbcReaderTest extends JdbcReaderTestDB {

	public DerbyJdbcReaderTest() {
		setJdbcConnectionFactory( new DerbyJdbcConnectionFactory() );
	}

}
