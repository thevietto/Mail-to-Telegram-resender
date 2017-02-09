package ru.ignatyev.dao;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class SubscribersDao {

    private final String dbName;

    public SubscribersDao(String dbName) throws SQLException {
        this.dbName = dbName;
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='subscribers';");
        if (!rs.next()) {
            stmt.executeUpdate("CREATE TABLE subscribers (ID INT PRIMARY KEY NOT NULL)");
        }
        stmt.close();
        connection.close();
    }

    public List<Long> getSubscribersList() {
        return null;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbName);
    }

}
