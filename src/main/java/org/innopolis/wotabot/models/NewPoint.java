package org.innopolis.wotabot.models;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
public class NewPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @ManyToOne
    Roommate roommate;

    Date createdAt;

    @OneToMany
    List<NewPointMessage> newPointMessageList;



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
        return newPointMessageList;
    }

    public void setMessageList(List<NewPointMessage> newPointMessageList) {
        this.newPointMessageList = newPointMessageList;
    }

    @Override
    public String toString() {
        return "NewPoint{" +
                "id=" + id +
                ", roommate=" + roommate +
                ", createdAt=" + createdAt +
                ", messageList=" + newPointMessageList +
                '}';
    }
}
