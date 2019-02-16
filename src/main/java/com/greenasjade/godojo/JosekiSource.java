package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;


@NodeEntity
public class JosekiSource {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(JosekiSource.class);

    @Id @GeneratedValue Long id;
    public Long getId() {return id;}

    @Property
    private Long contributor;
    public Long getContributor() {return contributor;}

    @Property
    private String url;
    public String getUrl() {return url;}

    @Property
    private String description;
    public String getDescription() {return description;}

    public JosekiSource() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

    public JosekiSource(String description, String url, Long contributor) {
        this.url = url;
        this.description = description;
        this.contributor = contributor;
    }

    public String toString() {
        return this.description;
    }
} 
