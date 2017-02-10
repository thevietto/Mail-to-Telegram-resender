package ru.ignatyev.dao;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;

public class SubscribersDao {

    private final JdbcTemplate jdbcTemplate;

    public SubscribersDao(String dbName) throws SQLException {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + dbName);
        jdbcTemplate = new JdbcTemplate(ds);

        List<String> res = jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='subscribers'", String.class);
        if (res.isEmpty()) {
            jdbcTemplate.update("CREATE TABLE subscribers (ID INT PRIMARY KEY NOT NULL)");
        }
    }

    public List<Long> getSubscribersList() {
        return jdbcTemplate.queryForList("SELECT id FROM subscribers", Long.class);
    }

    public boolean subscribe(Long chatId) {
        List<Long> ids = jdbcTemplate.queryForList("SELECT id FROM subscribers WHERE ID = ?", Long.class, chatId);
        return ids.isEmpty() && jdbcTemplate.update("INSERT INTO subscribers(ID) VALUES (?)", chatId) > 0;
    }


    public boolean unsubscribe(Long chatId) {
        return jdbcTemplate.update("DELETE FROM subscribers WHERE ID = ?", chatId) > 0;
    }
}
