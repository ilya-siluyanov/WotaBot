package org.innopolis.wotabot.models;

import javax.persistence.*;
import java.util.Date;

@Entity
public class NewPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;

    @ManyToOne
    Roommate roommate;

    Date createdAt;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
