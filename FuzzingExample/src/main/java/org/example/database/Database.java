
package org.example.database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.User;
import org.h2.jdbcx.JdbcDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Nico Werner Keller
 * @version 1.0
 * @since 15.01.2023
 *
 * The class is a simple implementation of a database that uses a connection to an H2 in-memory database.
 * It has methods to connect, create, update, read, delete and close the database.
 */
public class Database {

    //Connection variable to connect to the H2 in-memory database.
    private Connection conn;

    // A string variable that holds the SQL statement to initialize the database.
    private final String initialize = "CREATE TABLE IF NOT EXISTS users (id IDENTITY PRIMARY KEY, username VARCHAR(50), password VARCHAR(50))";

    private Logger log = LogManager.getLogger(Database.class);

    /**
     * Method to connect to the H2 in-memory database, initialize the table and insert some data into it.
     */
    public void connect()  {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:database.db");
        try {
            conn = ds.getConnection();

            // A dummy database is dynamically created
            conn.createStatement().execute(initialize);
            conn.createStatement().execute("INSERT INTO users (username, password) VALUES ('admin', 'pa33word')");
            conn.createStatement().execute("INSERT INTO users (username, password) VALUES ('maxmustermann', 'password')");



        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * Method to insert a new user in the DB
     * @param user A User object representing the user to be created
     * @return A boolean value to indicate the success of the operation.
     */
    public Boolean create(User user) {
        String query;
        query = String.format("SELECT id FROM users WHERE username = '%s'", user.getUsername().toLowerCase());
        try {
            ResultSet resultSet = conn.createStatement().executeQuery(query);
            if(resultSet.next()){
                return false;
            } else {
                try {
                    log.info(String.format("Inserting User '%s' into database", user.getUsername()));
                    query = String.format("INSERT INTO users (username, password) VALUES ('%s', '%s')", user.getUsername(), user.getPassword());
                    return !conn.createStatement().execute(query);
                } catch (SQLException e){
                    log.error(String.format("Error in inputting '%s' into database", user.getUsername()));
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * This method takes two User object as input, the first one being the new user, and the second one being the old user. It updates the old user
     * information with the new user information - including the username and the password - in the database, it returns true if the user is successfully updated, otherwise false.
     * @param newuser A User object representing new user
     * @param olduser A User object representing the old user
     * @return
     */
    public Boolean update(User newuser, User olduser){
        try {
            log.info(String.format("updating User '%s'", olduser.getUsername().toLowerCase()));
            String query = String.format("UPDATE users SET username = '%s', password = '%s' WHERE username = '%s' AND password = '%s'", newuser.getUsername().toLowerCase(), newuser.getPassword(), olduser.getUsername().toLowerCase(), olduser.getPassword());
            conn.createStatement().executeUpdate(query);

        } catch (SQLException e){
            log.error(String.format("Error updating '%s'", olduser.getUsername().toLowerCase()));
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * This method checks the database to see if the provided username and password match a record in the users table.
     * @param username The username to be checked
     * @param password The password to be checked
     * @return The ResultSet of the query containing the user(s) if there is a match, otherwise null
     * @throws SQLException if there is an error executing the query
     */
    public ResultSet exists(String username, String password){
        String query = String.format("SELECT * FROM users WHERE username = '%s' and password = '%s'", username.toLowerCase(), password);
        try {
            return conn.createStatement().executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method retrieves all records from the users table in the database.
     * @return The ResultSet of the query
     * @throws SQLException if there is an error executing the query
     */
    public ResultSet readAll() {
        String query = String.format("SELECT * FROM users");
        try {
            return conn.createStatement().executeQuery(query);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method removes the user from the database table with the provided username.
     *
     * @param username The username of the user to be deleted
     * @throws SQLException if there is an error executing the query
     */
    public void delete(String username) {
        try {
            String query = String.format("DELETE FROM users WHERE name = '%s'", username.toLowerCase());
            conn.createStatement().execute(query);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    /**
     * This method closes the connection to the database.
     */
    public void close(){
        try {
            conn.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
    }


}
