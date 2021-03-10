package org.innopolis.wotabot.models;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class NewPoint {
    @Id
    int id;

    @ManyToOne
    Roommate roommate;

    Date createdAt;

    @OneToMany(fetch = FetchType.EAGER)
    List<NewPointMessage> messageList;

    public NewPoint() {
    }

    public NewPoint(int id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Roommate getRoommate() {
        return roommate;
    }

    public void setRoommate(Roommate roommate) {
        this.roommate = roommate;
    }

    public List<NewPointMessage> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<NewPointMessage> newPointMessageList) {
        this.messageList = newPointMessageList;
    }

    @Override
    public String toString() {
        return "NewPoint{" +
                "id=" + id +
                ", roommate=" + roommate +
                ", createdAt=" + createdAt +
                ", messageList=" + messageList +
                '}';
    }
}
