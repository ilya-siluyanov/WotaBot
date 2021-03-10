package org.innopolis.wotabot.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class NewPointMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    long chatId;

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