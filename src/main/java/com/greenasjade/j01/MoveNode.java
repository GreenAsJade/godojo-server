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
public class MoveNode {
	
	@Transient 
	private static final Logger log = LoggerFactory.getLogger(MoveNode.class);
	
	@Id @GeneratedValue private Long id;
	
	@Property("play")
	private String play;  // the string of moves to get here (later will be board hash?) 
	
	@Property("placement")
	private String placement;
	
	
	public String getPlacement() {
		return placement;
	}
	
	public MoveNode() {
		// Empty constructor required as of Neo4j API 2.0.5
	};

	public MoveNode(String placement, String play) {
		this.play = play;
		this.placement = placement;
	}
	
	public void setPlay(String play) {
		this.play=play;
	}
    public String getPlay() {
        return play;
    }
    
    @Property("count")
    public long child_count =0 ;
    
    @Relationship("CHILD")
    public Set<MoveNode> children;
    
    public String toString() {
    	return this.child_count + " :" + this.play + 
    			" -> " + Optional.ofNullable(this.children).orElse(
				Collections.emptySet()).stream()
				.map(MoveNode::getPlacement)
				.collect(Collectors.toList());
    }
    
	public MoveNode addMove(String placement) {
		this.child_count = this.child_count+1;
		MoveNode child = new MoveNode(placement, this.play+"."+placement);
		if (children == null) {
			children = new HashSet<>();
		}
		children.add(child);
		log.info("Added move, now: " + this.toString());
		return child;
	}
} 
