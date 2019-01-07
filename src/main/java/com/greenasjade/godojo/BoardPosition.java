package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Transient;


@NodeEntity
public class BoardPosition {
	
	@Transient 
	private static final Logger log = LoggerFactory.getLogger(BoardPosition.class);
	
	@Id @GeneratedValue Long id;
	
	@Property("play")
		private String play;  // the string of moves to get here 
    
    @Relationship("PARENT")
    public Set<Move> children;
    
    @Relationship(type="CHILD")
    public Move parent;
    	
	public BoardPosition() {
		// Empty constructor required as of Neo4j API 2.0.5
	}

	public BoardPosition(String play) {
		this.play = play;
	}
	
	public void setParent(Move parent) {
		this.parent=parent;
	}
	public void setPlay(String play) {
		this.play=play;
	}
	
    public String getPlay() {
        return play;
    }

    public String toString() {
    	String p = this.parent==null ? "." : this.parent.getPlacement();
    	
    	String i = this.id==null ? "tbd" : this.id.toString();
    	
    	return p + " -> " +  "<"+i+">" + this.play + 
    			" -> " + Optional.ofNullable(this.children).orElse(
				Collections.emptySet()).stream()
				.map(Move::getPlacement)
				.collect(Collectors.toList());
    }
    
	public BoardPosition addMove(String placement) {
		BoardPosition child = new BoardPosition(this.play + "." + placement);
		Move link = new Move(this, placement, child);
		if (children == null) {
			children = new HashSet<>();
		}
		children.add(link);
		//log.info("Added move: " + link.toString()); 
		//log.info("now this node: " + this.toString());
		return child;
	}
} 
