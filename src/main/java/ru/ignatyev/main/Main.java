package ru.ignatyev.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;

import ru.ignatyev.service.MailService;
import ru.ignatyev.service.TelegramService;

public class Main {

    public static Properties MAIN_PROPERTIES;

    public static void main(String[] args) throws Exception {
        MAIN_PROPERTIES = new Properties();
        try {
            MAIN_PROPERTIES.load(new FileInputStream("settings.properties"));
        } catch (IOException e) {
            System.out.println("Settings file not found");
            System.exit(0);
        }

        MailService mailService = new MailService();
        System.out.println("Log in to mail account");
        mailService.login(MAIN_PROPERTIES.getProperty("gmail.host"),
                MAIN_PROPERTIES.getProperty("gmail.username"),
                MAIN_PROPERTIES.getProperty("gmail.password"));
        System.out.println("Connected successfully!");

        System.out.println("Starting telegram bot");
        TelegramService telegramService = new TelegramService();
        System.out.println("Bot created successfully!");

        Integer interval = Integer.valueOf(MAIN_PROPERTIES.getProperty("mail.fetch.interval"));
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
