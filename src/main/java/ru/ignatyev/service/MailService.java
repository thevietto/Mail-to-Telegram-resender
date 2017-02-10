package ru.ignatyev.service;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IdleManager;
import ru.ignatyev.main.MessageProcessor;

import javax.mail.*;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MailService {

    private Session session;
    private IMAPFolder folder;

    private ExecutorService es;

    public void login(String host, String username, String password)
            throws Exception {
        String protocol = "imaps";
        String file = "INBOX";
        URLName url = new URLName(protocol, host, 993, file, username, password);
        es = Executors.newCachedThreadPool();
        if (session == null) {
            Properties props;
            try {
                props = System.getProperties();
                props.put("mail.event.scope", "session");
                props.put("mail.event.executor", es);
                props.put("mail.imaps.usesocketchannels", "true");
            } catch (SecurityException sex) {
                props = new Properties();
            }
            session = Session.getInstance(props, null);
        }
        Store store = session.getStore(url);
        store.connect();
        folder = (IMAPFolder) store.getFolder(url);

        folder.open(Folder.READ_WRITE);
    }

    public void addMessageListener(MessageProcessor processor) throws IOException, MessagingException {
        final IdleManager idleManager = new IdleManager(session, es);
        folder.addMessageCountListener(new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent event) {
                Message[] messages = event.getMessages();
                for (Message message : messages) {
                    processor.process(message);
                }
                try {
                    idleManager.watch(folder);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });
        idleManager.watch(folder);
    }

}