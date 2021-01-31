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
}
