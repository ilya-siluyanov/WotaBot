package org.innopolis.wotabot.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class NewPointMessage {
    @Id
    int id;

    long chatId;

    public NewPointMessage() {
    }

    public NewPointMessage(long messageId, long chatId) {
        this.chatId = chatId;
    }

    public int getId() {
        return id;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }
}