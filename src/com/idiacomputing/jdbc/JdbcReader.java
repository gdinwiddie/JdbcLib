/*
 * JdbcReader
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

/**
 * An abstract class for reading from the database.
 * 
 * <p>
 * Example:
 * 
 * <pre>
 * JdbcReader reader = new JdbcReader() {
 *     protected Connection fetchConnection() throws SQLException {
 *         return createConnection();
 *     }
 * 
 *     protected void readRowValue(ResultSet resultSet) throws SQLException {
 *         String name = resultSet.getString(1);
 *         long value = resultSet.getLong(2);
 *         storeValues(name, value);
 *     }
 * 
 *     protected String fetchSelectStatement() {
 *         return defaultSql;
 *     }
 * };
 * reader.readDb();
 * </pre>
 * 
 * See JdbcReaderTestDB for more examples of usage
 * 
 * @author George Dinwiddie
 *  
 */
public abstract class JdbcReader {

    /**
     * Override this method to provide a database connection.
     * 
     * @exception SQLException
     *                for problems opening Connection
     */
    abstract protected Connection fetchConnection() throws SQLException;

    /**
     * Override this method to provide the SQL SELECT statement for reading from
     * the database.
     */
    abstract protected String fetchSelectStatement();

    /**
     * Override this method to bind variables to the select statement.
     * <p>
     * If there are any bind variables in the SQL Statement provided by
     * fetchSelectStatement(), it is imperative that you override
     * bindSelectStatement() to provide values for those bind variables.
     * 
     * @exception SQLException
     *                if the PreparedStatement rejects any of the bindings.
     */
    protected void bindSelectStatement(PreparedStatement preparedStatement)
            throws SQLException {
    }

    /**
     * Override this method to process a <b>single <b>row retrieved from the
     * database.
     * <p>
     * In general, the ResultSet should only be read, and not affected in any
     * other way. An exception to this is calling resultSet.next() to verify
     * that only one row was returned, where that is required.
     * <p>
     * In cases where multiple rows are expected, this method will be called for
     * each row returned by the query.
     * 
     * @exception SQLException
     *                if the ResultSet access is incorrect.
     */
    abstract protected void readRowValue(ResultSet resultSet)
            throws SQLException;

    /**
     * Override this method to log exceptions that occur while releasing JDBC
     * resources.
     * <p>
     * The default behavior is to silently swallow these exceptions.
     */
    protected void logUnthrownException(String message, Exception caught) {
    }

    /**
     * This is the method that does the work. Call this, and it will call the
     * methods you've overriden.
     * 
     * @exception SQLException
     *                for problems accessing the database.
     */
    public final void readDb() throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = fetchConnection();
            String selectStatement = fetchSelectStatement();

            statement = connection.prepareStatement(selectStatement);
            bindSelectStatement(statement);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                readRowValue(resultSet);
            }
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException caught) {
                logUnthrownException("Exception closing ResultSet", caught);
            }
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException caught) {
                logUnthrownException("Exception closing PreparedStatement",
                        caught);
            }
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException caught) {
                logUnthrownException("Exception closing Connection", caught);
            }
        }
    }

}