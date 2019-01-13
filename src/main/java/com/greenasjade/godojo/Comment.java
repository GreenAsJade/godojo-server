package com.greenasjade.godojo;

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
	private String text;
	
	@Relationship("COMMENT")
	BoardPosition target;
	
	public Comment() {
		// Empty constructor required as of Neo4j API 2.0.5
	};

	public Comment(BoardPosition parent, String text) {
		this.target = parent;
		this.text = text;
	}
	    
    public String toString() {
        return this.text;
    }
} 
