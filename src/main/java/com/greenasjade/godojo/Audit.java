package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;


@NodeEntity
public class Audit {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(Audit.class);

    @Id @GeneratedValue Long id;

    @Property
    private Instant date;
    public Instant getDate() {return date;}

    @Property
    private Long user_id;
    public Long getUserId() {return user_id;}

    @Property
    private String comment;
    public String getComment() {return comment;}

    @Property
    private String field;
    public String getField() {return field;}

    @Property
    private String original_value;
    public String getOriginalValue() {return original_value;}

    @Property
    private String new_value;
    public String getNewValue() {return new_value;}

    @Relationship("AUDIT")
    BoardPosition ref;

    public Audit() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public Audit(BoardPosition ref, String field, String from, String to, String comment, Long user_id) {
        this.ref = ref;
        this.user_id = user_id;
        this.field = field;
        this.original_value = from;
        this.new_value = to;
        this.comment = comment;
        this.date = Instant.now();
    }

    public String toString() {
        if (this.ref == null) {
            log.info("** Audit has null ref");
        }
        if (this.field != "") {
            return String.format("User %s changed %s from %s to %s, %s",
                    this.user_id.toString(),
                    this.field,
                    this.original_value,
                    this.new_value,
                    this.comment);
        }
        else {
            return String.format("User %s %s", this.user_id.toString(), this.comment);
        }
    }
} 
