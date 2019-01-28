package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Transient;

enum PlayCategory {
	IDEAL, GOOD, MISTAKE, TRICK, QUESTION
}

@NodeEntity
public class BoardPosition {
	
	@Transient 
	private static final Logger log = LoggerFactory.getLogger(BoardPosition.class);
	
	@Id @GeneratedValue Long id;
	
	@Property("play")
	private String play;  // the string of moves to get here 
    public String getPlay() {return play;}
    
    @Property("placement")
    private String placement; // the move played to get here.  Easier than stripping it off the play.
    public String getPlacement() {return placement;}
    
	@Property("seq")
	public Integer seq; // what order to display this one in relative to others
	
	@Property("category")
	private PlayCategory category;
	public PlayCategory getCategory() {return category;}
    
	@Property("title")
	private String title;  
    public String getTitle() {return title;}
    public void setTitle(String text) {title = text;}
    
    @Property("Description")
	private String description;
	public String getDescription() {return description;}
	public void setDescription(String text) {description = text;}
	
    @Relationship(type="PARENT", direction = Relationship.INCOMING)
    public List<BoardPosition> children;
    
    @Relationship(type="PARENT", direction = Relationship.OUTGOING)
    public BoardPosition parent;
    public void setParent(BoardPosition parent) { this.parent = parent;}
    	
	@Relationship(type="JOSEKI_POSITION", direction = Relationship.INCOMING)
	private Set<Joseki> joseki;    
    
	@Relationship("COMMENT")
    public ArrayList<Comment> commentary;

    public BoardPosition() {
		// Empty constructor required as of Neo4j API 2.0.5
	}

	public BoardPosition(String parent_play, String placement) {
		this(parent_play, placement, PlayCategory.IDEAL);
	}
	
	public BoardPosition(String parent_play, String placement, PlayCategory category) {
		this.play = parent_play + "." + placement;
		this.placement = placement;
		this.category = category;
		this.title = "";  // these get set during editing, after the position is created.
		this.description = "";
		this.children = new ArrayList<>();
		this.commentary = new ArrayList<>();			
	}
	
    public void addJoseki(Joseki joseki) {
		if (this.joseki == null) {
			this.joseki = new HashSet<>();
		}    	
    	this.joseki.add(joseki);
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
    	
    	String c = this.commentary == null ? "" : this.commentary.toString();
    	
    	String child_list = 
    			Optional.ofNullable(this.children).orElse(Collections.emptyList()).stream()
				.map(BoardPosition::getPlacement)
				.collect(Collectors.toList()).toString();
    			
    	return p + " -> " +  "<" + i +">" + this.play + 
    			" -> " + child_list + c;
    }
    
    public BoardPosition addMove(String placement) {
    	return this.addMove(placement, PlayCategory.IDEAL);
    }
    
	public BoardPosition addMove(String placement, PlayCategory category) {
		if (this.children == null) {
			this.children = new ArrayList<>();
		}

		BoardPosition child = new BoardPosition(this.play, placement, category);
		
		children.add(child);
		child.setParent(this);
		child.seq = this.children.size();
		log.info("Added move: " + placement); 
		log.info("now this node: " + this.toString());
		return child;
	}
} 
