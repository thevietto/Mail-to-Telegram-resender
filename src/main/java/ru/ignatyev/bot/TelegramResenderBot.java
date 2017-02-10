package ru.ignatyev.bot;

import org.telegram.telegrambots.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import ru.ignatyev.dao.SubscribersDao;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;


public class TelegramResenderBot extends TelegramLongPollingBot {

    private final String botUsername;
    private final String botToken;
    private SubscribersDao subscribersDao;

    private Map<String, Consumer<Message>> actions = new HashMap<String, Consumer<Message>>() {{
        put("/subscribe", m -> {
            SendMessage message = new SendMessage().setChatId(m.getChatId()).setReplyToMessageId(m.getMessageId());
            message.setChatId(m.getChatId());
            if (subscribersDao.subscribe(m.getChatId())) {
                message.setText("You are successfully subscribed to google form updates!");
            } else {
                message.setText("There was an error when subscribing. Possibly you are subscribed already");
            }
            sendMessageSafe(message);
        });
        put("/unsubscribe", m -> {
            SendMessage message = new SendMessage().setChatId(m.getChatId()).setReplyToMessageId(m.getMessageId());
            if (subscribersDao.unsubscribe(m.getChatId())) {
                message.setText("You are successfully unsubscribed");
            } else {
                message.setText("There was an error when unsubscribing. Possibly you were not subscribed yet");
            }
            sendMessageSafe(message);
        });
        put("test", m -> {
            broadcast("TEST TEXT");
        });
    }};

//    private Map<String, Consumer<>> callbacks

    public TelegramResenderBot(String botUsername, String botToken, String dbName) throws SQLException {
        this.botUsername = botUsername;
        this.botToken = botToken;
        subscribersDao = new SubscribersDao(dbName);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Message updateMessage = update.getMessage() == null ? update.getChannelPost() : update.getMessage();
        if (updateMessage != null) {
            Consumer<Message> action = actions.get(updateMessage.getText());
            if (action != null) {
                action.accept(updateMessage);
            }
            return;
        }

        CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery == null || callbackQuery.getMessage() == null || callbackQuery.getData() == null) {
            return;
        }

        callbackQuery.getFrom().getId(); // user
        callbackQuery.getMessage().getMessageId(); //message
        callbackQuery.getData(); // command
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery()
                .setCallbackQueryId(callbackQuery.getId()).setText("Cool! \uD83D\uDC4D");
        try {
            answerCallbackQuery(answerCallbackQuery);
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
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        buttons.add(new InlineKeyboardButton().setText("\uD83D\uDC4D").setCallbackData("/like"));
        buttons.add(new InlineKeyboardButton().setText("\uD83D\uDC4E").setCallbackData("/dislike"));
        inlineKeyboardMarkup.setKeyboard(Collections.singletonList(buttons));
        for (Long chatId : subscribersList) {
            SendMessage message = new SendMessage();
            message.setChatId(chatId)
                    .setText(messageText)
                    .setReplyMarkup(inlineKeyboardMarkup);
            sendMessageSafe(message);
        }
    }

    private void sendMessageSafe(SendMessage message) {
        try {
            sendMessage(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
