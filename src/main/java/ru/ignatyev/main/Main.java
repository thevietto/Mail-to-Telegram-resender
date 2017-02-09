package ru.ignatyev.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;

import ru.ignatyev.service.MailService;
import ru.ignatyev.service.TelegramService;

public class Main {

    public static Properties SETTINGS;

    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        SETTINGS = new Properties();
        try {
            SETTINGS.load(new FileInputStream("settings.properties"));
        } catch (IOException e) {
            System.out.println("Settings file not found");
            System.exit(0);
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

        Integer interval = Integer.valueOf(SETTINGS.getProperty("mail.fetch.interval"));
        System.out.println("Starting mail catcher. [Interval " + interval + " seconds]");
        ScheduledExecutorService mailCatcher = Executors.newScheduledThreadPool(1);
        mailCatcher.scheduleAtFixedRate(() -> {
            try {
                Message[] messages = mailService.getUnreadMessages();
                for (Message message : messages) {
                    System.out.println("Found new message!");
                    telegramService.processMessage(message);
                }
            } catch (Exception e) {
                System.out.println("There was an error when processing a message!");
                e.printStackTrace();
                mailCatcher.shutdown();
            }

        }, 0, interval, TimeUnit.SECONDS);
    }

}
