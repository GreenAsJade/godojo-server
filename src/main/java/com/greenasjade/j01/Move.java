package com.greenasjade.j01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Transient;


@RelationshipEntity("CHILD")
public class Move {
	
	@Transient 
	private static final Logger log = LoggerFactory.getLogger(Move.class);
	
	@Id @GeneratedValue private Long id;
	
	@Property("placement")
	private String placement;
	
	@StartNode
	BoardPosition parent;
	
	@EndNode
	BoardPosition child;
	
	public String getPlacement() {
		return placement;
	}
	
	public Move() {
		// Empty constructor required as of Neo4j API 2.0.5
	};

	public Move(BoardPosition parent, String placement, BoardPosition child) {
		this.parent = parent;
		this.placement = placement;
		this.child = child;
	}
	    
    public String toString() {
    	if (this.parent == null || this.placement == null ||  this.child == null) {
    		return "(move tbd)";
        }
    	else {
    		return this.parent.getPlay() + " -> " + 
        			this.placement + " -> "+ 
        			this.child.getPlay();
        }
    }
} 
