package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NodeEntity
public class User {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(User.class);

    @Id @GeneratedValue Long id;

    @Property
    private Long user_id;
    public Long getUserId() {return this.user_id;}

    @Property
    private Boolean can_edit;
    public Boolean canEdit() { return this.can_edit; }
    public void setCanEdit(Boolean val) { this.can_edit = val; }

    @Property
    private Boolean administrator;
    public Boolean isAdministrator() { return this.administrator; }
    public void setAdministrator(Boolean val) { this.administrator =  val; }

    public User() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    User(Long user_id) {
        this.user_id = user_id;
        this.can_edit = false;
        this.administrator = false;
    }

    public String toString() {
        return ("User: " + this.user_id.toString() + " " + this.can_edit + " " + this.administrator);
    }
}
