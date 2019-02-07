package com.greenasjade.godojo;

import java.io.Serializable;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;


@NodeEntity
public class Comment {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(Comment.class);

    @Id @GeneratedValue Long id;

    @Property
    private Instant date;
    public Instant getDate() {return date;}

    @Property
    private Integer user_id;
    public Integer getUserId() {return user_id;}

    @Property
    private String comment;
    public String getComment() {return comment;}

    @Relationship("COMMENT")
    BoardPosition target;

    public Comment() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public Comment(BoardPosition parent, String comment, Integer user_id) {
        this.target = parent;
        this.user_id = user_id;
        this.comment = comment;
        this.date = Instant.now();
    }

    public String toString() {
        return this.comment;
    }
} 
