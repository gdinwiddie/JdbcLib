/*
 * JdbcWriter
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
import java.sql.SQLException;

/**
 * An abstract class for writing to the database. See JdbcWriterTestDB for
 * examples of usage
 * 
 * @author George Dinwiddie
 */
public abstract class JdbcWriter {
    //~ Instance fields
    // --------------------------------------------------------

    private boolean oneRowWritten;

    //~ Methods
    // ----------------------------------------------------------------

    /**
     * This is the method that does the work. Call this, and it will call the
     * methods you've overriden.
     */
    public final void writeDb() throws SQLException {
        reset();

        Connection connection = null;
        PreparedStatement statement = null;
        //ResultSet resultSet = null;
        SQLException firstException = null;

        try {
            connection = fetchConnection();

            String insertSql = fetchInsertStatement();

            statement = connection.prepareStatement(insertSql);

            while (hasMoreData()) {
                bindInsertStatement(statement);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            firstException = e;
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException caught) {
                if (firstException == null) {
                    firstException = caught;
                } else {
                    logUnthrownException("Exception closing PreparedStatement",
                            caught);
                }
            }

            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException caught) {
                if (firstException == null) {
                    firstException = caught;
                } else {
                    logUnthrownException("Exception closing Connection", caught);
                }
            }

            if (firstException != null) {
                throw firstException;
            }
        }
    }

    /**
     * Override this method to provide a database connection
     * 
     * @return SQL connection
     */
    protected abstract Connection fetchConnection() throws SQLException;

    /**
     * Override this method to provide the SQL INSERT or UPDATE statement for
     * writing to the database
     * 
     * @return SQL insert or update string
     */
    protected abstract String fetchInsertStatement();

    /**
     * Override this method to bind variables to the INSERT or UPDATE statement.
     * 
     * <p>
     * The default behavior does nothing, so if there are no bind variables in
     * the SQL statement, you do not have to override this method.
     * </p>
     * 
     * @param preparedStatement
     *            The prepared statement with bind variables
     */
    protected void bindInsertStatement(PreparedStatement preparedStatement)
            throws SQLException {
    }

    /**
     * Override this method to execute multiple inserts.
     * 
     * <p>
     * The default behavior (for convenience) is to bind once per instantiation
     * of the class. This is the simplest case and this method makes it easy to
     * write a single row.
     * </p>
     * 
     * <p>
     * When writing more than one row, override this method and use the tools of
     * your data source (array or Collection, generally) to tell if there's more
     * data to be written.
     * </p>
     * 
     * @return true if there are more rows to be written
     */
    protected boolean hasMoreData() {
        boolean returnValue = !oneRowWritten;

        oneRowWritten = true;

        return returnValue;
    }

    /**
     * Override this method to log exceptions that occur while releasing JDBC
     * resources.
     * 
     * @param message
     *            The message to log
     * @param caught
     *            The exception caught
     */
    protected void logUnthrownException(String message, Exception caught) {
    }

    /**
     * This method resets the writer for another use
     */
    protected void reset() {
        oneRowWritten = false;
    }
}