package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

@NodeEntity
public class User {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(User.class);

    @Id @GeneratedValue Long id;

    @Property
    private Long user_id;
    public Long getUserId() {return this.user_id;}
    public void setUserId(Long id) {this.user_id = id;}

    @Property
    private Boolean can_edit;
    public Boolean canEdit() { return this.can_edit; }
    public void setCanEdit(Boolean val) { this.can_edit = val; }

    @Property
    private Boolean administrator;
    public Boolean isAdministrator() { return this.administrator; }
    public void setAdministrator(Boolean val) { this.administrator = val; }

    @Property
    private Boolean can_comment;
    Boolean canComment() { return this.can_comment; }
    void setCanComment(Boolean val) { this.can_comment = val; }

    @Relationship(type = "PLAYED")
    public ArrayList<PlayRecord> played_josekis;

    @Transient  // We don't store this on our server, because it can change
    public String username;


    public User() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    User(Long user_id) {
        this.user_id = user_id;
        this.can_comment = true; // at the beginning, registered users can comment
        this.can_edit = false;
        this.administrator = false;
        this.played_josekis = new ArrayList<>();
    }

    public String toString() {
        String basic_info = String.format("User: %s (%s) comment %s edit %s admin %s played positions: ",
                this.user_id.toString(),
                String.valueOf(this.username),
                this.can_comment,
                this.can_edit,
                this.administrator);

        // its useful to know what positions were played, but full elaboration of each play record is too long
        return basic_info + (this.played_josekis == null ? "null" : this.played_josekis.stream()
                .map(r -> r.getPosition().id.toString()).collect(Collectors.joining(",")));
    }

    public Integer errorsFor(Long position_id) {
        PlayRecord played = this.played_josekis.stream()
                .filter(p -> p.getPosition().id == position_id)
                .findFirst()
                .orElse(null);

        return played == null ? 0 : played.getBest_attempt();
    }

    public Long completedJosekiCount() {
        return played_josekis.stream()
                .filter(p -> p.getSuccesses() > 0)
                .count();
    }

    public Integer josekisPlayedCount() {
        return played_josekis.size();
    }

}
