package ru.ignatyev.main;

import ru.ignatyev.service.MailService;
import ru.ignatyev.service.TelegramService;

import javax.mail.MessagingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        final Properties SETTINGS = new Properties();
        try {
            SETTINGS.load(new FileInputStream("settings.properties"));
        } catch (IOException e) {
            System.out.println("Settings file not found");
            System.exit(1);
        }

        MailService mailService = new MailService();
        System.out.println("Log in to mail account");
        mailService.login(SETTINGS.getProperty("gmail.host"),
                SETTINGS.getProperty("gmail.username"),
                SETTINGS.getProperty("gmail.password"));
        System.out.println("Connected successfully!");

        System.out.println("Starting telegram bot");
        TelegramService telegramService = new TelegramService();
        try {
            telegramService.setUp(SETTINGS.getProperty("telegram.bot.username"),
                    SETTINGS.getProperty("telegram.bot.token"),
                    SETTINGS.getProperty("sqllite3.db.name"));
        } catch (Exception e) {
            System.out.println("There was an error when creating a bot");
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Bot created successfully!");

        System.out.println("Starting mail catcher");
        mailService.addMessageListener(message -> {
            System.out.println("Message received!");
            try {
                telegramService.processMessage(message);
            } catch (IOException | MessagingException e) {
                System.out.println("There was ad error when processing message");
                e.printStackTrace();
            }
        });
        System.out.println("Mail catcher started!");
    }

}
