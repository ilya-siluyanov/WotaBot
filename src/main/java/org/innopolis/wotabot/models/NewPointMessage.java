package org.innopolis.wotabot.models;

import javax.persistence.*;

@Entity
public class NewPointMessage {

    /**
     * id = chatId + " " + messageId;
     */
    @Id
    String id;

    @ManyToOne
    NewPoint newPoint;

    public NewPointMessage() {

    }

    public NewPointMessage(long messageId, long chatId) {
        this.id = messageId + " " + chatId;
    }

    public String getId() {
        return id;
    }

    public long getChatId() {
        return Long.parseLong(id.split(" ")[1]);
    }

    public int getMessageId() {
        return Integer.parseInt(id.split(" ")[0]);
    }

    public NewPoint getNewPoint() {
        return newPoint;
    }

    public void setNewPoint(NewPoint newPoint) {
        this.newPoint = newPoint;
    }
}