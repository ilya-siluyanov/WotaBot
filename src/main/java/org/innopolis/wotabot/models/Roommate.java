package org.innopolis.wotabot.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Roommate {
    @Id
    private String userName;

    private String realName;
    //how many times the user brought water and trash
    private int points;

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
}
