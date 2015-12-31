/*
 * MockSingleRowJdbcWriter
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

import junit.framework.AssertionFailedError;

import org.easymock.MockControl;

/**
 * MockSingleRowJdbcWriter
 * 
 * @author $Author: cvs $
 * @version $Revision: 1.1 $
 */
public class MockSingleRowJdbcWriter extends JdbcWriter {
    private MockControl connectionControl;

    private MockControl preparedStatementControl;

    private int timesBound = 0;

    public void verify() {
        verifyConnection();
        verifyPreparedStatement();
    }

    protected void verifyPreparedStatement() throws AssertionFailedError {
        getPreparedStatementControl().verify();
    }

    protected void verifyConnection() throws AssertionFailedError {
        getConnectionControl().verify();
    }

    protected MockControl fetchPreparedStatementControl() {
        setPreparedStatementControl(MockControl
                .createControl(PreparedStatement.class));
        return getPreparedStatementControl();
    }

    protected MockControl fetchConnectionControl() {
        setConnectionControl(MockControl.createControl(Connection.class));
        return getConnectionControl();
    }

    protected Connection makeMockConnection() throws SQLException {
        setConnectionControl(fetchConnectionControl());
        Connection mockConnection = (Connection) getConnectionControl()
                .getMock();

        mockConnection.prepareStatement("INSERT INTO JdbcWriterTestTable "
                + "(name, value) VALUES (?, ?)");
        getConnectionControl().setReturnValue(makeMockPreparedStatement());
        mockConnection.close();
        getConnectionControl().replay();
        return mockConnection;
    }

    protected void bindInsertStatement(PreparedStatement preparedStatement)
            throws SQLException {
        setTimesBound(getTimesBound() + 1);
        preparedStatement.setString(1, "neener");
        preparedStatement.setLong(2, 69L);
    }

    protected PreparedStatement makeMockPreparedStatement() throws SQLException {
        setPreparedStatementControl(fetchPreparedStatementControl());
        PreparedStatement mockPreparedStatement = (PreparedStatement) getPreparedStatementControl()
                .getMock();

        mockPreparedStatement.setString(1, "neener");
        mockPreparedStatement.setLong(2, 69L);
        mockPreparedStatement.executeUpdate();
        getPreparedStatementControl().setReturnValue(1, 1);
        mockPreparedStatement.close();
        getPreparedStatementControl().replay();
        return mockPreparedStatement;
    }

    protected Connection fetchConnection() throws SQLException {
        return makeMockConnection();
    }

    protected String fetchInsertStatement() {
        return "INSERT INTO JdbcWriterTestTable "
                + "(name, value) VALUES (?, ?)";
    }

    protected void setConnectionControl(MockControl connectionControl) {
        this.connectionControl = connectionControl;
    }

    protected MockControl getConnectionControl() {
        return connectionControl;
    }

    protected void setPreparedStatementControl(
            MockControl preparedStatementControl) {
        this.preparedStatementControl = preparedStatementControl;
    }

    protected MockControl getPreparedStatementControl() {
        return preparedStatementControl;
    }

    protected void setTimesBound(int timesBound) {
        this.timesBound = timesBound;
    }

    protected int getTimesBound() {
        return timesBound;
    }
}