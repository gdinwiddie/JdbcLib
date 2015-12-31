# JdbcLib

Today, JDBC seems out of style. People seem to prefer ORM tools that automatically wire Java objects to database tables.

I prefer to do such mapping intentionally, as I often find that there are differences between what I prefer in a domain object model and a database schema. This library handles the drudgery and boilerplate of JDBC, leaving me to customize the parts that need to be customized.

I wrote the first version of this library circa 2001, when I was on a project where the JDBC code was different every place it was written. And, where JDBC objects often weren't closed properly in some cases, causing resource leaks in the application server. You can see this concern for bulletproof operation in the unit test suite. This was also my introduction to Mock Objects, when I used a very early version of EasyMock to replace the bulky hand-rolled fakes I was creating for JDBC classes.

At my next job, I needed to write a Data Access Object, so I wrote this library again. It was the next time, when I was consulting with my first client, that I wised up and wrote it on my own time, releasing it using an MIT-styled license. I made it available on my website, but now I'm posting it on GitHub.

## Design and Usage

The library mainly consists of two abstract template classes, `JdbcReader` for read operations, and `JdbcWriter` for create, update, and delete operations. These are typically encapsulated within a Data Access Adapter class that provides an application-centric interface, catches and handles any `SQLException`s, and provides common services (such as a `JdbcConnectionFactory`) to all of the Data Access Objects to whom it delegates.

### JdbcReader

Subclasses need to override `fetchConnection()` to hand a `Connection` to the database. This is generally delegated to a `JdbcConnectionFactory` since all the connections are generally created in the same manner or retrieved from the same connection pool.

Override `fetchSelectStatement()` to provide a string version of the SQL `Select` statement. This might be a constant string, or it might be the output of a "builder" method that uses string fragments common to related DAOs.

Override `bindSelectStatement()` if you have any bind variables in your `Select` statement.

Override `readRowValue()` to unpack one row of the returned `ResultSet`. The base class will handle the looping. In this method, you'll copy data from the `ResultSet` into your domain object. I've often found it convenient to use a join that covers both parent and child objects, creating the parent if it doesn't exist (e.g., not in a local `HashMap`), and adding a child for each row.

If you want, override `logUnthrownException()` to put messages into your normal logging system. `JdbcReader` will throw any `SQLException` encountered in operation, but only after closing all the JDBC objects. If another exception is thrown during these closes, it will be reported by calling `logUnthrownException()` but otherwise ignored.

Add a method to be called by the Data Access Adapter that receives the bind parameters, calls `readDb()`, and returns the domain object(s) into which the results have been stored.

### JdbcWriter

Subclasses need to override `fetchConnection()` the same as for `JdbcReader`, probably using the same `JdbcConnectionFactory`.

Override `fetchInsertStatement()` to provide a string version of the SQL `Insert`, `Update`, or `Delete` statement.

Override `bindInsertStatement()` if you have any bind variables in your `Insert`, `Update`, or `Delete` statement. This will be called repeatedly for all the data that needs to be written.

By default, `hasMoreData()` will halt the write after a `bindInsertStatement()`, but if you've got a collection of bind variables, override this method to signal when you've reached the end.

Again, override `logUnthrownException()` to put messages into your normal logging system. `JdbcReader` will throw any `SQLException` encountered in operation, but only after closing all the JDBC objects. If another exception is thrown during these closes, it will be reported by calling `logUnthrownException()` but otherwise ignored.

Add a method to be called by the Data Access Adapter that receives the domain objects to be written, calls `reset()` in case you're reusing an instance, and calls `writeDb()`.

