package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A tag is some property that applies to a board position, such as "Position is settled" etc
// (could also have been called "label" except we already have "variation_label" which is quite different)

@NodeEntity
public class Tag {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(Tag.class);

    @Id @GeneratedValue Long id;
    public Long getId() {return id;}

    @Property
    private String description;
    public String getDescription() {return description;}

    public Tag() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public Tag(String description) {
        this.description = description;
    }

    public String toString() {
        return this.description;
    }
} 
