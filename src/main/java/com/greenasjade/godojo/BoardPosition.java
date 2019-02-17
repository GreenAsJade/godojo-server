package com.greenasjade.godojo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
    public void setCategory(PlayCategory category) {this.category = category;}

    @Property("description")
    private String description;
    public String getDescription() {return description;}
    public void setDescription(String text) {description = text;}

    @Property("contributor")
    private Long contributor_id;
    public Long getContributorId(){return contributor_id;}
    public void setContributorId(Long id) {contributor_id = id;}

    @Property("labels")
    private List<String> labels;
    public List<String> getLabels() {return labels;}

    @Relationship(type="PARENT", direction = Relationship.INCOMING)
    public List<BoardPosition> children;

    @Relationship(type="PARENT", direction = Relationship.OUTGOING)
    public BoardPosition parent;
    public void setParent(BoardPosition parent) { this.parent = parent;}

    @Relationship("COMMENT")
    public ArrayList<Comment> commentary;

    public Integer getCommentCount() {return commentary != null ? commentary.size() : 0;}

    @Relationship("SOURCE")
    public JosekiSource source;
    public Long getJosekiSourceId() {return this.source != null ? this.source.id : 0;}

    public BoardPosition() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    public BoardPosition(String parent_play, String placement, Long user_id) {
        this(parent_play, placement, PlayCategory.IDEAL, user_id);
    }

    public BoardPosition(String parent_play, String placement, PlayCategory category, Long user_id) {
        this.play = parent_play + "." + placement;
        this.placement = placement;
        this.category = category;
        this.contributor_id = user_id;

        this.description = "";
        this.children = new ArrayList<>();
        this.commentary = new ArrayList<>();
        log.info(placement + " created with " + commentary.size() + " comments");

        this.source = null;
    }

    public void addComment(String text, Long user_id) {
        Comment new_comment = new Comment(this, text, user_id);
        if (this.commentary == null) {
            this.commentary = new ArrayList<>();
        }
        this.commentary.add(new_comment);
    }

    public String toString() {
        String p = this.parent==null ? "." : this.parent.getPlacement();

        String i = this.id==null ? "tbd" : this.id.toString();

        String c = this.commentary == null ? "" : this.commentary.toString();

        String u = this.contributor_id.toString();

        String child_list =
                Optional.ofNullable(this.children).orElse(Collections.emptyList()).stream()
                        .map(BoardPosition::getPlacement)
                        .collect(Collectors.toList()).toString();

        return p + " -> " +  "<"+i+">" + "("+u+")"+ this.play +
                " -> " + child_list + c;
    }

    public BoardPosition addMove(String placement, Long user_id) {
        return this.addMove(placement, PlayCategory.IDEAL, user_id);
    }

    public BoardPosition addMove(String placement, PlayCategory category, Long user_id) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }

        // Take care not to add a new child if we already have one at this placement
        // must only have one child of each placement

        BoardPosition existing = this.children.stream()
                .filter( c -> c.getPlacement().equals(placement) )
                .findFirst().orElse(null);

        if (existing == null) {
            BoardPosition child = new BoardPosition(this.play, placement, category, user_id);

            children.add(child);
            child.setParent(this);
            child.seq = this.children.size();
            log.info("Added move: " + placement);
            log.info("now this node: " + this.toString());
            return child;
        }
        else {
            log.warn("Attempted to add an existing position.  Updating instead. " + placement);
            existing.category = category;
            return existing;
        }
    }
} 
