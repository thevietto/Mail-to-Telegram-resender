package ru.ignatyev.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

import ru.ignatyev.dao.SubscribersDao;


public class TelegramResenderBot extends TelegramLongPollingBot {

    private static final String SUBSCRIBE_COMMAND = "/subscribe";

    private final String botUsername;
    private final String botToken;
    private SubscribersDao subscribersDao;

    public TelegramResenderBot(String botUsername, String botToken, String dbName) throws SQLException {
        this.botUsername = botUsername;
        this.botToken = botToken;
        subscribersDao = new SubscribersDao(dbName);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message updateMessage = update.getMessage();
        if (updateMessage == null) {
            updateMessage = update.getChannelPost();
        }
        if (updateMessage == null) {
            return;
        }
        if (SUBSCRIBE_COMMAND.equals(updateMessage.getText())) {
            SendMessage message = new SendMessage();
            message.setChatId(updateMessage.getChatId());
            message.setText("You are successfully subscribed to google form updates!");
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void broadcast(String messageText) {
        List<Long> subscribersList = subscribersDao.getSubscribersList();
        for (Long chatId : subscribersList) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(messageText);
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
