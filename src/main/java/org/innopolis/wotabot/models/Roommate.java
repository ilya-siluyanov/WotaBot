package org.innopolis.wotabot.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Objects;

@Entity
public class Roommate {
    @Id
    private String userName;

    private String realName;
    //how many times the user brought water and trash
    private int points;

    private long chatId;

    @OneToMany(mappedBy = "roommate")
    List<NewPoint> newPointList;

    public String getRealName() {
        return realName;
    }

    public int getPoints() {
        return points;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    @Override
    public String toString() {
        return "Roommate{" +
                "userName:'" + userName + '\'' +
                ", realName:'" + realName + '\'' +
                ", points:" + points +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Roommate roommate = (Roommate) o;
        return userName.equals(roommate.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }
}
