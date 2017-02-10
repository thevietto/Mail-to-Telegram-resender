package ru.ignatyev.service;


import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.ignatyev.bot.TelegramResenderBot;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;

public class TelegramService {

    private TelegramResenderBot bot;

    public void setUp(String botName, String botToken, String dbName) throws TelegramApiRequestException, SQLException {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        bot = new TelegramResenderBot(botName, botToken, dbName);
        botsApi.registerBot(bot);
    }

    public void processMessage(Message message) throws IOException, MessagingException {
        bot.broadcast(message.getContent().toString());
    }

}
