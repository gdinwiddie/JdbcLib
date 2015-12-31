/*
 * JdbcWriterTestDB
 * 
 * Copyright(c) 2002 by George Dinwiddie
 * Copyright(c) 2005 iDIA Computing, LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms,
 * with or without modification, are permitted
 * provided that the following conditions are met:
 * <UL>
 * <LI>Redistributions of source code must retain
 *     the above copyright notice, this list of
 *     conditions and the following disclaimer.
 * <LI>Redistributions in binary form must reproduce
 *     the above copyright notice, this list of conditions
 *     and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 * <LI>Neither the name of the iDIA Computing, LLC
 *     nor the names of its contributors may be used
 *     to endorse or promote products derived from this
 *     software without specific prior written permission.
 * </UL>
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
package com.idiacomputing.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author George Dinwiddie
 *  
 */
public abstract class JdbcWriterTestDB extends TestCase {
    private JdbcConnectionFactory jdbcConnectionFactory;

    private String defaultSql = "INSERT INTO TestTableJdbcWriterTestDB (name, value) VALUES (?, ?)";

    private void createTestTable() throws SQLException {
        String sql = "Create table TestTableJdbcWriterTestDB ( "
                + "NAME   VARCHAR  (40)  NOT NULL, "
                + "VALUE  INTEGER        NOT NULL)";
        Connection connection = createConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.execute();
        statement.close();
        connection.close();
    }

    private void dropTestTable() throws SQLException {
        String sql = "Drop table TestTableJdbcWriterTestDB";
        Connection connection = createConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.execute();
        statement.close();
        connection.close();
    }

    private int countRows() throws SQLException {
        String sql = "SELECT count(*) FROM TestTableJdbcWriterTestDB";
        Connection connection = createConnection();
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet resultSet = statement.executeQuery();

        assertTrue("no row returned", resultSet.next());
        int count = resultSet.getInt(1);

        assertTrue("extra row returned", !resultSet.next());
        resultSet.close();
        statement.close();
        connection.close();
        return count;
    }

    private Map readRows() throws SQLException {
        final Map map = new HashMap();
        JdbcReader reader = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                String name = resultSet.getString(1);
                long value = resultSet.getLong(2);

                map.put(name, new Long(value));
            }

            protected String fetchSelectStatement() {
                return "SELECT name, value FROM TestTableJdbcWriterTestDB";
            }

        };

        reader.readDb();
        return map;
    }

    protected void setUp() throws Exception {
        createTestTable();
    }

    protected void tearDown() throws Exception {
        dropTestTable();
    }

    public void testEmptyTable() throws Exception {
        JdbcWriter uut = new JdbcWriter() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected String fetchInsertStatement() {
                return defaultSql;
            }

            protected boolean hasMoreData() {
                return false;
            }

        };

        uut.writeDb();
        assertEquals(0, countRows());
    }

    public void testSimpleRow() throws Exception {
        JdbcWriter uut = new JdbcWriter() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected String fetchInsertStatement() {
                return "INSERT INTO TestTableJdbcWriterTestDB (name, value) VALUES ('borg', 987)";
            }
        };

        uut.writeDb();
        assertEquals(1, countRows());
        Map data = readRows();

        assertEquals(new Long(987L), data.get("borg"));
    }

    public void testOneRow() throws Exception {
        JdbcWriter uut = new JdbcWriter() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected String fetchInsertStatement() {
                return defaultSql;
            }

            protected void bindInsertStatement(
                    PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, "n'est ce pas");
                preparedStatement.setInt(2, 31);
            }

        };

        uut.writeDb();
        assertEquals(1, countRows());
        Map data = readRows();

        assertEquals(new Long(31L), data.get("n'est ce pas"));
    }

    public void testMultipleRows() throws Exception {
        JdbcWriter uut = new JdbcWriter() {
            String[] names = { "one", "two", "three" };

            int[] values = { 1, 2, 3 };

            int fetchCounter = 0;

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected String fetchInsertStatement() {
                return defaultSql;
            }

            protected void bindInsertStatement(
                    PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setString(1, names[fetchCounter]);
                preparedStatement.setInt(2, values[fetchCounter]);
                fetchCounter++;
            }

            protected boolean hasMoreData() {
                return fetchCounter < names.length;
            }

        };

        uut.writeDb();
        assertEquals(3, countRows());
        Map data = readRows();

        assertEquals(new Long(1L), data.get("one"));
        assertEquals(new Long(2L), data.get("two"));
        assertEquals(new Long(3L), data.get("three"));
    }

    public void testUpdate() throws Exception {
        testMultipleRows();
        JdbcWriter uut = new JdbcWriter() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected String fetchInsertStatement() {
                return "UPDATE TestTableJdbcWriterTestDB SET value = ? WHERE name = ?";
            }

            protected void bindInsertStatement(
                    PreparedStatement preparedStatement) throws SQLException {
                preparedStatement.setInt(1, 123);
                preparedStatement.setString(2, "two");
            }

        };

        uut.writeDb();
        Map data = readRows();

        assertEquals(new Long(123L), data.get("two"));
        assertEquals(new Long(1L), data.get("one"));
        assertEquals(new Long(3L), data.get("three"));
    }

    /**
     * creates a local database connection
     * 
     * expects property file to specify db user name and password. can also
     * override driver and url.
     */
    private Connection createConnection() throws SQLException {
        return jdbcConnectionFactory.getConnection();
    }

	protected void setJdbcConnectionFactory(JdbcConnectionFactory connectionFactory) {
		jdbcConnectionFactory = connectionFactory;
	}

}