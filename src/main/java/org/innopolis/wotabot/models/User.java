package org.innopolis.wotabot.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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
}
