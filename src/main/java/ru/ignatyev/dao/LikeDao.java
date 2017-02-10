package ru.ignatyev.dao;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;

public class LikeDao {

    private final JdbcTemplate jdbcTemplate;

    public LikeDao(String dbName) throws SQLException {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + dbName);
        jdbcTemplate = new JdbcTemplate(ds);

        List<String> res = jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='like_event'", String.class);
        if (res.isEmpty()) {
            jdbcTemplate.update("CREATE TABLE like_event (" +
                    "  USER_ID INT NOT NULL," +
                    "  CHAT_ID INT NOT NULL," +
                    "  MESSAGE_ID INT NOT NULL," +
                    "  EVENT INT NOT NULL," +
                    "  PRIMARY KEY (USER_ID, CHAT_ID, MESSAGE_ID)" +
                    ")");
        }
    }

    public boolean isAlready(LikeEventType eventType, Integer userId, Long chatId, Integer messageId) {
        return !jdbcTemplate.queryForList("SELECT event FROM like_event WHERE user_id = ? AND chat_id = ? AND message_id = ? AND event = ?",
                Long.class, userId, chatId, messageId, eventType.ordinal()).isEmpty();
    }

    public synchronized boolean createEvent(LikeEventType eventType, Integer userId, Long chatId, Integer messageId) {
        return jdbcTemplate.update("INSERT INTO like_event (user_id, chat_id, message_id, event) VALUES (?, ?, ?, ?)",
                userId, chatId, messageId, eventType.ordinal()) > 0;
    }

    public synchronized boolean removeEvent(LikeEventType eventType, Integer userId, Long chatId, Integer messageId) {
        return jdbcTemplate.update("DELETE FROM like_event WHERE user_id = ? AND chat_id = ? AND  message_id = ? AND event = ?",
                userId, chatId, messageId, eventType.ordinal()) > 0;
    }

    public synchronized Long count(LikeEventType eventType, Long chatId, Integer messageId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(event) FROM like_event WHERE chat_id = ? AND message_id = ? AND event = ?",
                Long.class, chatId, messageId, eventType.ordinal());
    }

}
