/*
 * JdbcReaderTestDB 
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

public abstract class JdbcReaderTestDB extends TestCase {
    private JdbcConnectionFactory jdbcConnectionFactory;

    private Map valuesRead = null;

    private String defaultSql = "Select name, value from TestTableJdbcReadDaoTestDB";

    private void creatTestTable() throws SQLException {
        String sql = "Create table TestTableJdbcReadDaoTestDB ( "
                + "NAME   VARCHAR  (40)  NOT NULL, "
                + "VALUE  INTEGER        NOT NULL )";
        Connection connection = createConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.execute();
        statement.close();
        connection.close();
    }

    private void dropTestTable() throws SQLException {
        String sql = "Drop table TestTableJdbcReadDaoTestDB";
        Connection connection = createConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.execute();
        statement.close();
        connection.commit();
        connection.close();
    }

    private void addRow(String name, long value) throws SQLException {
        String[] names = { name };
        long[] values = { value };

        addRows(names, values);
    }

    private void addRows(String[] names, long[] values) throws SQLException {
        String sql = "INSERT INTO TestTableJdbcReadDaoTestDB (name, value) VALUES (?, ?)";
        Connection connection = createConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        for (int index = 0; index < names.length; index++) {
            statement.setString(1, names[index]);
            statement.setLong(2, values[index]);
            statement.execute();
        }
        statement.close();
        connection.close();
    }

    public void setUp() throws Exception {
        creatTestTable();
        valuesRead = new HashMap();
    }

    public void tearDown() throws Exception {
        valuesRead = null;
        dropTestTable();
    }

    public void testEmptyTable() throws Exception {
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                String name = resultSet.getString(1);
                long value = resultSet.getLong(2);

                storeValues(name, value);
            }

            protected String fetchSelectStatement() {
                return defaultSql;
            }

        };

        uut.readDb();
        assertTrue(valuesRead.isEmpty());
    }

    public void testOneRow() throws Exception {
        addRow("borogrove", 256L);
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                String name = resultSet.getString(1);
                long value = resultSet.getLong(2);

                storeValues(name, value);
            }

            protected String fetchSelectStatement() {
                return defaultSql;
            }

        };

        uut.readDb();
        assertTrue(valuesRead.containsKey("borogrove"));
        assertEquals(new Long(256L), valuesRead.get("borogrove"));
        assertEquals(1, valuesRead.size());
    }

    public void testThreeRows() throws Exception {
        String[] names = { "borogrove", "mimsy", "outgrabe" };
        long[] values = { 256L, 99L, -1L };

        addRows(names, values);
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                String name = resultSet.getString(1);
                long value = resultSet.getLong(2);

                storeValues(name, value);
            }

            protected String fetchSelectStatement() {
                return defaultSql;
            }

        };

        uut.readDb();
        for (int index = 0; index < names.length; index++) {
            assertEquals(new Long(values[index]), valuesRead.get(names[index]));
        }
        assertEquals(3, valuesRead.size());
    }

    public void testOneOfThreeRows() throws Exception {
        String[] names = { "borogrove", "mimsy", "outgrabe" };
        long[] values = { 256L, 99L, -1L };

        addRows(names, values);
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                String name = resultSet.getString(1);
                long value = resultSet.getLong(2);

                storeValues(name, value);
            }

            protected String fetchSelectStatement() {
                return defaultSql + " WHERE name = 'mimsy'";
            }

        };

        uut.readDb();
        assertEquals(new Long(99L), valuesRead.get("mimsy"));
        assertEquals(1, valuesRead.size());
    }

    private class MyVariableBindingJdbcReader extends JdbcReader {
        public String selectedName = "";

        protected Connection fetchConnection() throws SQLException {
            return createConnection();
        }

        protected void readRowValue(ResultSet resultSet) throws SQLException {
            String name = resultSet.getString(1);
            long value = resultSet.getLong(2);

            storeValues(name, value);
        }

        protected String fetchSelectStatement() {
            return defaultSql + " WHERE name = ?";
        }

        protected void bindSelectStatement(PreparedStatement preparedStatement)
                throws SQLException {
            preparedStatement.setString(1, selectedName);
        }

    }

    public void testVariableBinding() throws Exception {
        String[] names = { "borogrove", "mimsy", "outgrabe" };
        long[] values = { 256L, 99L, -1L };

        addRows(names, values);
        MyVariableBindingJdbcReader uut = new MyVariableBindingJdbcReader();

        for (int i = 0; i < names.length; i++) {
            uut.selectedName = names[i];
            valuesRead.clear();
            uut.readDb();
            assertEquals(new Long(values[i]), valuesRead.get(names[i]));
            assertEquals(1, valuesRead.size());
        }
    }

    private boolean rowFound = false;

    public void testRowExists() throws Exception {
        rowFound = false;
        String[] names = { "borogrove", "mimsy", "outgrabe" };
        long[] values = { 256L, 99L, -1L };

        addRows(names, values);
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                rowFound = true;
            }

            protected String fetchSelectStatement() {
                return defaultSql + " WHERE name = 'mimsy'";
            }

        };

        uut.readDb();
        assertTrue(rowFound);
    }

    private boolean oneAndOnlyOneRowFound = false;

    public void testOneAndOnlyOneRowFound() throws Exception {
        oneAndOnlyOneRowFound = false;
        String[] names = { "borogrove", "mimsy", "outgrabe" };
        long[] values = { 256L, 99L, -1L };

        addRows(names, values);
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                if (!resultSet.next()) {
                    oneAndOnlyOneRowFound = true;
                }
            }

            protected String fetchSelectStatement() {
                return defaultSql + " WHERE name = 'mimsy'";
            }

        };

        uut.readDb();
        assertTrue(oneAndOnlyOneRowFound);
    }

    public void testSqlException() throws Exception {
        addRow("borogrove", 256L);
        JdbcReader uut = new JdbcReader() {

            protected Connection fetchConnection() throws SQLException {
                return createConnection();
            }

            protected void readRowValue(ResultSet resultSet)
                    throws SQLException {
                throw new SQLException("failure in readRowValue()");
            }

            protected String fetchSelectStatement() {
                return defaultSql;
            }

        };

        try {
            uut.readDb();
            fail("Should throw SQLException");
        } catch (SQLException expected) {
        }
    }

    private void storeValues(String name, long value) {
        Long number = new Long(value);

        valuesRead.put(name, number);
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