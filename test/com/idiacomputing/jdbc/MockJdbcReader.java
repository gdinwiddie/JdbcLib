/*
 * MockJdbcReader
 * 
 * Copyright(c) 2002 by George Dinwiddie Copyright(c) 2005 iDIA Computing, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <UL><LI> Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer. <LI>
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution. <LI> Neither the name
 * of the iDIA Computing, LLC nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior
 * written permission. </UL>
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *  
 */
package com.idiacomputing.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import junit.framework.AssertionFailedError;

import org.easymock.MockControl;

public class MockJdbcReader extends JdbcReader {
    private MockControl connectionControl;

    private MockControl preparedStatementControl;

    private MockControl resultSetControl;

    public MockJdbcReader() {
    }

    protected ResultSet makeMockResultSet() throws SQLException {
        fetchResultSetControl();
        ResultSet mockResultSet = (ResultSet) resultSetControl.getMock();

        mockResultSet.next();
        resultSetControl.setReturnValue(true, 3);
        resultSetControl.setReturnValue(false);
        mockResultSet.close();
        resultSetControl.replay();
        return mockResultSet;
    }

    protected MockControl fetchResultSetControl() {
        resultSetControl = MockControl.createControl(ResultSet.class);
        return resultSetControl;
    }

    protected PreparedStatement makeMockPreparedStatement() throws SQLException {
        preparedStatementControl = fetchPreparedStatementControl();
        PreparedStatement mockPreparedStatement = (PreparedStatement) preparedStatementControl
                .getMock();

        mockPreparedStatement.setString(1, "nada");
        mockPreparedStatement.executeQuery();
        preparedStatementControl.setReturnValue(makeMockResultSet());
        mockPreparedStatement.close();
        preparedStatementControl.replay();
        return mockPreparedStatement;
    }

    protected MockControl fetchPreparedStatementControl() {
        preparedStatementControl = MockControl
                .createControl(PreparedStatement.class);
        return preparedStatementControl;
    }

    protected MockControl fetchConnectionControl() {
        connectionControl = MockControl.createControl(Connection.class);
        return connectionControl;
    }

    protected Connection makeMockConnection() throws SQLException {
        connectionControl = fetchConnectionControl();
        Connection mockConnection = (Connection) connectionControl.getMock();

        mockConnection.prepareStatement("SELECT * from DUAL WHERE nada = ?");
        connectionControl.setReturnValue(makeMockPreparedStatement());
        mockConnection.close();
        connectionControl.replay();
        return mockConnection;
    }

    protected Connection fetchConnection() throws SQLException {
        return makeMockConnection();
    }

    private boolean selectStatementWasFetched = false;

    protected String fetchSelectStatement() {
        selectStatementWasFetched = true;
        return "SELECT * from DUAL WHERE nada = ?";
    }

    private boolean selectStatementWasBound = false;

    protected void bindSelectStatement(PreparedStatement preparedStatement)
            throws SQLException {
        selectStatementWasBound = true;
        preparedStatement.setString(1, "nada");
    }

    private int rowsFetched = 0;

    protected void readRowValue(ResultSet resultSet) {
        ++rowsFetched;
    }

    public void verify() {
        verifyConnection();
        verifyStatementFetched();
        verifyStatementBound();
        if (rowsFetched != 3) {
            throw new AssertionFailedError(rowsFetched
                    + " rows fetched, expected 3");
        }
        verifyPreparedStatement();
        resultSetControl.verify();
    }

    protected void verifyPreparedStatement() throws AssertionFailedError {
        preparedStatementControl.verify();
    }

    protected void verifyStatementFetched() throws AssertionFailedError {
        if (!selectStatementWasFetched) {
            throw new AssertionFailedError("select Statement wasn't fetched");
        }
    }

    protected void verifyStatementBound() throws AssertionFailedError {
        if (!selectStatementWasBound) {
            throw new AssertionFailedError(
                    "select Statement variables weren't bound");
        }
    }

    protected void verifyConnection() throws AssertionFailedError {
        connectionControl.verify();
    }

}