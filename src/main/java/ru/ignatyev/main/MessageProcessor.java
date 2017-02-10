package ru.ignatyev.main;

import javax.mail.Message;

public interface MessageProcessor {

    void process(Message message);

}
