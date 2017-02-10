package ru.ignatyev.service;


import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import ru.ignatyev.bot.TelegramResenderBot;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

import java.io.IOException;
import java.sql.SQLException;

import com.overzealous.remark.Remark;

public class TelegramService {

    private TelegramResenderBot bot;
    private Remark remark;

    public void setUp(String botName, String botToken, String dbName) throws TelegramApiRequestException, SQLException {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        bot = new TelegramResenderBot(botName, botToken, dbName);
        botsApi.registerBot(bot);
        remark = new Remark();
    }

    public void processMessage(Message message) throws IOException, MessagingException {
        Object content = message.getContent();
        String result;
        if (content instanceof MimeMultipart) {
            MimeMultipart mimeMultipart = (MimeMultipart) content;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mimeMultipart.getCount(); i++) {
                BodyPart bodyPart = mimeMultipart.getBodyPart(i);
                if (bodyPart.getContentType().startsWith("TEXT/PLAIN")) {
                    sb.append(bodyPart.getContent().toString());
                }
            }
            result = sb.toString();
        } else {
            result = content.toString();
        }
        bot.broadcast(remark.convert(result));
    }

}
