package ru.ignatyev.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.List;

public class MessageDao {

    private final JdbcTemplate jdbcTemplate;

    public MessageDao(String dbName) throws SQLException {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.sqlite.JDBC");
        ds.setUrl("jdbc:sqlite:" + dbName);
        jdbcTemplate = new JdbcTemplate(ds);

        List<String> res = jdbcTemplate.queryForList(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='messages'", String.class);
        if (res.isEmpty()) {
            jdbcTemplate.update("CREATE TABLE messages (" +
                    "  chat_id INT NOT NULL," +
                    "  message_id INT NOT NULL," +
                    "  rating INT NOT NULL DEFAULT 0," +
                    "  PRIMARY KEY (chat_id, message_id)" +
                    ")");
        }
    }

    public boolean createMessage(Long chatId, Integer messageId) {
        return jdbcTemplate.update("INSERT INTO messages(chat_id, message_id) VALUES (?, ?)",
                chatId, messageId) > 0;
    }

    public synchronized Long getRating(Long chatId, Integer messageId) {
        return jdbcTemplate.queryForObject("SELECT rating FROM messages WHERE chat_id = ? AND message_id = ?",
                Long.class, chatId, messageId);
    }

    public synchronized boolean incrementRating(Long chatId, Integer messageId) {
        Long rating = getRating(chatId, messageId);
        rating = rating + 1;
        return jdbcTemplate.update("UPDATE messages SET rating = ? WHERE chat_id = ? AND message_id = ?",
                rating, chatId, messageId) > 0;
    }

    public synchronized boolean decrementRating(Long chatId, Integer messageId) {
        Long rating = getRating(chatId, messageId);
        rating = rating + 1;
        return jdbcTemplate.update("UPDATE messages SET rating = ? WHERE chat_id = ? AND message_id = ?",
                rating, chatId, messageId) > 0;
    }

}
