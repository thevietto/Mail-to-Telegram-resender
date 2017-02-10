package ru.ignatyev.bot;

import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.ignatyev.dao.SubscribersDao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public class TelegramResenderBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;
    private SubscribersDao subscribersDao;

    private Map<String, Function<Message, SendMessage>> actions = new HashMap<String, Function<Message, SendMessage>>() {{
        put("/subscribe", updateMessage -> {
            SendMessage message = new SendMessage();
            message.setChatId(updateMessage.getChatId());
            if (subscribersDao.subscribe(updateMessage.getChatId())) {
                message.setText("You are successfully subscribed to google form updates!");
            } else {
                message.setText("There was an error when subscribing. Possibly you are subscribed already");
            }
            return message;
        });
        put("/unsubscribe", updateMessage -> {
            SendMessage message = new SendMessage();
            if (subscribersDao.unsubscribe(updateMessage.getChatId())) {
                message.setText("You are successfully unsubscribed");
            } else {
                message.setText("There was an error when unsubscribing. Possibly you were not subscribed yet");
            }
            return message;
        });
    }};

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
        Function<Message, SendMessage> action = actions.get(updateMessage.getText());
        if (action == null) {
            return;
        }
        SendMessage message = action.apply(updateMessage);
        message.setChatId(updateMessage.getChatId());
        message.setReplyToMessageId(updateMessage.getMessageId());
        try {
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
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
