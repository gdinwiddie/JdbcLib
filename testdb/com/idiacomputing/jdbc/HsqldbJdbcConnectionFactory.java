/*
 * LocalJdbcConnectionFactory.
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * LocalJdbcConnectionFactory
 * 
 * @author $Author: cvs $
 * @version $Revision: 1.1 $
 */
public class HsqldbJdbcConnectionFactory implements JdbcConnectionFactory {
    String jdbcUser = null;

    public Connection getConnection() throws SQLException {
        jdbcUser = "sa"; // "dbUserName";
        String jdbcPassword = ""; // "dbUserPassword";
        String jdbcDriver = "org.hsqldb.jdbcDriver"; // "oracle.jdbc.driver.OracleDriver";
        String jdbcUrl = "jdbc:hsqldb:file:DBforTesting/hsqldb"; // "jdbc:oracle:thin:@machine:port:name";
        Properties jdbcProperties = new Properties();
        String filename = "resources/jdbc/jdbc.properties";

        try {
            InputStream propertyFile = new FileInputStream(filename);

            jdbcProperties.load(propertyFile);
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
        jdbcUser = jdbcProperties.getProperty("jdbcUser", jdbcUser);
        jdbcPassword = jdbcProperties.getProperty("jdbcPassword", jdbcPassword);
        jdbcDriver = jdbcProperties.getProperty("jdbcDriver", jdbcDriver);
        jdbcUrl = jdbcProperties.getProperty("jdbcUrl", jdbcUrl);
        try {
            Class.forName(jdbcDriver); // load the driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver '" + jdbcDriver + "' not found.");
        }
        Connection localConnection;

        localConnection = DriverManager.getConnection(jdbcUrl, jdbcUser,
                jdbcPassword);
        localConnection.setAutoCommit(true);
        return localConnection;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

	public void loadDriver(String jdbcDriver) throws SQLException {
	}
}