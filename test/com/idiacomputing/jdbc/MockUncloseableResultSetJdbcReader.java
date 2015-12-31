/*
 * MockUncloseableResultSetJdbcReader
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import junit.framework.AssertionFailedError;

import org.easymock.MockControl;

public class MockUncloseableResultSetJdbcReader extends MockJdbcReader {
    private Collection exceptionsThrown;

    private Collection exceptionsLogged;

    public MockUncloseableResultSetJdbcReader() {
        super();
        exceptionsThrown = new HashSet();
        exceptionsLogged = new HashSet();
    }

    protected void logUnthrownException(String message, Exception caught) {
        exceptionsLogged.add(caught);
    }

    private void verifyExceptions() throws AssertionFailedError {
        Iterator it = exceptionsThrown.iterator();

        while (it.hasNext()) {
            Exception thrown = (Exception) it.next();

            if (!exceptionsLogged.contains(thrown)) {
                throw new AssertionFailedError("Unlogged Exception: "
                        + thrown.getMessage());
            }
        }
    }

    public void verify() {
        super.verify();
        verifyExceptions();
    }

    protected ResultSet makeMockResultSet() throws SQLException {
        MockControl resultSetControl = fetchResultSetControl();
        ResultSet mockResultSet = (ResultSet) resultSetControl.getMock();

        mockResultSet.next();
        resultSetControl.setReturnValue(true, 3);
        resultSetControl.setReturnValue(false);
        mockResultSet.close();
        SQLException e = new SQLException("broken ResultSet");

        exceptionsThrown.add(e);
        resultSetControl.setThrowable(e);
        resultSetControl.replay();
        return mockResultSet;
    }

}