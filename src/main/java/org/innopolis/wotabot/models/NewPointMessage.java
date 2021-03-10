package org.innopolis.wotabot.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class NewPointMessage {

    /**
     * id = chatId + " " + messageId;
     */
    @Id
    String id;

    public NewPointMessage() {

    }

    public NewPointMessage(long messageId, long chatId) {
        this.id = messageId + " " + chatId;
    }

    public String getId() {
        return id;
    }

    public long getChatId() {
        return Long.parseLong(id.split(" ")[0]);
    }

    public int getMessageId() {
        return Integer.parseInt(id.split(" ")[1]);
    }
}