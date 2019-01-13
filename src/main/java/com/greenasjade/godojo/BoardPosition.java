package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    public String getPlay() {return play;}
    
	@Property("title")
	private String title;  
    public String getTitle() {return title;}
    public void setTitle(String text) {title = text;}
    
    @Property("Description")
	private String description;
	public String getDescription() {return description;}
	public void setDescription(String text) {description = text;}
	
    @Relationship("PARENT")
    public Set<Move> children;
    
    @Relationship(type="CHILD")
    public Move parent;
    public void setParent(Move parent) { this.parent = parent;}
    	
	@Relationship("COMMENT")
    public ArrayList<Comment> commentary;

    public BoardPosition() {
		// Empty constructor required as of Neo4j API 2.0.5
	}

	public BoardPosition(String play) {
		this.play = play;
		this.title = "";  // these get set during editing, after the position is created.
		this.description = "";
	}
	
	public void addComment(String text) {
		Comment new_comment = new Comment(this, text);
		if (this.commentary == null) {
			this.commentary = new ArrayList<Comment>();			
		}
		this.commentary.add(new_comment);
	}

    public String toString() {
    	String p = this.parent==null ? "." : this.parent.getPlacement();
    	
    	String i = this.id==null ? "tbd" : this.id.toString();
    	
    	String c = this.commentary==null ? "''" : this.commentary.toString();
    	
    	String child_list = 
    			Optional.ofNullable(this.children).orElse(Collections.emptySet()).stream()
				.map(Move::getPlacement)
				.collect(Collectors.toList()).toString();
    			
    	return p + " -> " +  "<" + i +">" + this.play + 
    			" -> " + child_list + c;
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
