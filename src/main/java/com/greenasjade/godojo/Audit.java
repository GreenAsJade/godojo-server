package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

enum ChangeType {
    CREATED,
    CATEGORY_CHANGE,
    DESCRIPTION_CHANGE,
    SOURCE_CHANGE,
    ADD_CHILD,
    REMOVE_CHILD,
    DEACTIVATE
}

@NodeEntity
public class Audit {

    @Transient
    private static Long audit_count = 0L;

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
    private ChangeType type;
    public ChangeType getType() {return type;}

    @Property
    private String comment;
    public String getComment() {return comment;}

    @Property
    private String original_value;
    public String getOriginalValue() {return original_value;}

    @Property
    private String new_value;
    public String getNewValue() {return new_value;}

    @Property
    // a number guaranteed to be in order of audit creation, used implicitly in pageable audit query
    private Long seq;
    public Long getSeq() {return seq;}

    @Relationship("AUDIT")
    BoardPosition ref;

    public Audit() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public Audit(BoardPosition ref, ChangeType type, String from, String to, String comment, Long user_id) {
        this.ref = ref;
        this.user_id = user_id;
        this.original_value = from;
        this.new_value = to;
        this.type = type;
        this.comment = comment;
        this.date = Instant.now();
        this.seq = audit_count;
        audit_count = audit_count + 1;
    }


    public Audit(BoardPosition ref, ChangeType type, String comment, Long user_id) {
        this.ref = ref;
        this.user_id = user_id;
        this.original_value = "";
        this.new_value = "";
        this.type = type;
        this.comment = comment;
        this.date = Instant.now();
        this.seq = audit_count;
        audit_count = audit_count + 1;
    }

    public String toString() {
        return String.format("User %s %s: %s", this.user_id.toString(), this.getType(), this.comment);
    }
} 
