package com.greenasjade.j01;

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
	
	@Id @GeneratedValue private Long id;
	
	@Property("play")
		private String play;  // the string of moves to get here 
	
    @Property("count")
    public long child_count = 0;
    
    @Relationship("PARENT")
    public Set<Move> children;
    
    @Relationship(type="CHILD", direction=Relationship.INCOMING)
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
    	String p = this.parent==null ? "(none)" : this.parent.getPlacement();
    	
    	return this.child_count + ": " + p + " -> " + this.play + 
    			" -> " + Optional.ofNullable(this.children).orElse(
				Collections.emptySet()).stream()
				.map(Move::getPlacement)
				.collect(Collectors.toList());
    }
    
	public BoardPosition addMove(String placement) {
		this.child_count = this.child_count+1;
		BoardPosition child = new BoardPosition(this.play + "." + placement);
		Move link = new Move(this, placement, child);
		if (children == null) {
			children = new HashSet<>();
		}
		children.add(link);
		log.info("Added move: " + link.toString()); 
		log.info("now this node: " + this.toString());
		return child;
	}
} 
