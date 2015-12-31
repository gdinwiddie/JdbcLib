/*
 * JdbcWriterTest
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

import java.sql.SQLException;

import junit.framework.TestCase;

public class JdbcWriterTest extends TestCase {

    public JdbcWriterTest(String name) {
        super(name);
    }

    public void testHappyPath() throws Exception {
        MockJdbcWriter uut = new MockJdbcWriter();

        uut.writeDb();
        uut.verify();
    }

    public void testSingleRowWriter() throws Exception {
        MockSingleRowJdbcWriter uut = new MockSingleRowJdbcWriter();
        uut.writeDb();
        uut.verify();
    }

    public void testReuseOfSingleRowWriter() throws Exception {
        MockSingleRowJdbcWriter uut = new MockSingleRowJdbcWriter();
        uut.writeDb();
        uut.verify();
        uut.writeDb();
        uut.verify();
    }

    public void testNoConnection() throws Exception {
        MockJdbcWriter uut = new MockNoConnectionJdbcWriter();

        try {
            uut.writeDb();
            fail("Should fail for no database connection");
        } catch (SQLException expected) {
        }
        uut.verify();
    }

    public void testBadConnection() throws Exception {
        MockJdbcWriter uut = new MockBadConnectionJdbcWriter();

        try {
            uut.writeDb();
            fail("Should fail for no PreparedStatement");
        } catch (SQLException expected) {
        }
        uut.verify();
    }

    public void testBadPreparedStatement() throws Exception {
        MockJdbcWriter uut = new MockBadPreparedStatementJdbcWriter();

        try {
            uut.writeDb();
            fail("Should fail for bad PreparedStatement");
        } catch (SQLException expected) {
        }
        uut.verify();
    }

    public void testFailureOnClose() throws Exception {
        MockJdbcWriter uut = new MockUncloseableConnectionJdbcWriter();
        try {
            uut.writeDb();
            fail("Should fail on exception thrown during close");
        } catch (SQLException expected) {
        }
        uut.verify();
    }
}