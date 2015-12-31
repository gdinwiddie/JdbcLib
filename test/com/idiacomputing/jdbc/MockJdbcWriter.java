/*
 * MockJdbcWriter
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

import java.sql.PreparedStatement;
import java.sql.SQLException;

import junit.framework.AssertionFailedError;

public class MockJdbcWriter extends MockSingleRowJdbcWriter {

    protected boolean hasMoreData() {
        if (getTimesBound() < 2) {
            return true;
        }
        return false;
    }

    protected void bindInsertStatement(PreparedStatement preparedStatement)
            throws SQLException {
        setTimesBound(getTimesBound() + 1);
        switch (getTimesBound()) {
        case 1:
            preparedStatement.setString(1, "nada");
            preparedStatement.setLong(2, 0);
            break;
        case 2:
            preparedStatement.setString(1, "floobydust");
            preparedStatement.setLong(2, 99L);
            break;
        default:
            throw new AssertionFailedError(
                    "Too many calls to bindInsertStatement()");
        }
    }

    protected PreparedStatement makeMockPreparedStatement() throws SQLException {
        setPreparedStatementControl(fetchPreparedStatementControl());
        PreparedStatement mockPreparedStatement = (PreparedStatement) getPreparedStatementControl()
                .getMock();

        mockPreparedStatement.setString(1, "nada");
        mockPreparedStatement.setLong(2, 0);
        mockPreparedStatement.executeUpdate();
        getPreparedStatementControl().setReturnValue(1, 1);
        mockPreparedStatement.setString(1, "floobydust");
        mockPreparedStatement.setLong(2, 99L);
        mockPreparedStatement.executeUpdate();
        getPreparedStatementControl().setReturnValue(1, 1);
        mockPreparedStatement.close();
        getPreparedStatementControl().replay();
        return mockPreparedStatement;
    }

}