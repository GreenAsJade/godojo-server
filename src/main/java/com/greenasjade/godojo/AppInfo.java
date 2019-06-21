package com.greenasjade.godojo;

import org.neo4j.ogm.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

/*
    A node in the DB where we store general app level info
    Initially created for storing schema version.
 */

@Data
@NodeEntity
public class AppInfo {

    @Transient
    private static final Logger log = LoggerFactory.getLogger(AppInfo.class);

    @Id @GeneratedValue Long id;
    public Long getId() {return id;}

    @Property
    private Integer schema_id;

    public AppInfo() {
        // Empty constructor required as of Neo4j API 2.0.5
    };

} 
