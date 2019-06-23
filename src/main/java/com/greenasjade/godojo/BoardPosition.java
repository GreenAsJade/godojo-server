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

    @Property("variation_label")
    private Character variation_label;
    public Character getVariationLabel() {return variation_label;}
    public void setVariationLabel(Character label) { variation_label = label; }

    @Property("category")
    private PlayCategory category;
    public PlayCategory getCategory() {return category;}
    public void setCategory(PlayCategory category, Long user_id) {
        if (this.category != category) {
            this.audits.add(new Audit(this, ChangeType.CATEGORY_CHANGE,  this.category.toString(), category.toString(),"Changed category to " + category.toString(), user_id));
            this.category = category;
        }
    }

    @Property("description")
    private String description;
    public String getDescription() {return description;}
    public void setDescription(String text, long user_id) {
        if (!text.equals(description)) {
            this.audits.add(new Audit(this, ChangeType.DESCRIPTION_CHANGE, description, text, "Changed description", user_id));
            description = text;
        }
    }

    @Property("contributor")
    private Long contributor_id;
    public Long getContributorId(){return contributor_id;}
    public void setContributorId(Long id) {contributor_id = id;}

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

    @Relationship("TAGS")
    public ArrayList<Tag> tags;

    @Relationship("AUDIT")
    public ArrayList<Audit> audits;

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
        this.tags = new ArrayList<>();

        log.info(placement + " created by user " + user_id.toString());

        this.source = null;

        this.audits = new ArrayList<>();
        this.audits.add(new Audit(this, ChangeType.CREATED,"Created", user_id));
    }

    public void addComment(String text, Long user_id) {
        Comment new_comment = new Comment(this, text, user_id);
        if (this.commentary == null) {
            this.commentary = new ArrayList<>();
        }
        this.commentary.add(new_comment);
        // Use "old value" to save which comment this is.
        this.audits.add(new Audit(this, ChangeType.ADD_COMMENT, String.valueOf(this.commentary.size()), text, "Commented", user_id));
    }

    public String toString() {
        return this.play;
    }

    public String getInfo() {
        String p = this.parent==null ? "." : this.parent.getPlacement();

        String i = this.id==null ? "tbd" : this.id.toString();

        String c = this.commentary==null ? "" : this.commentary.toString();

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

        // Take care not to add a new child if we already have one at this placement:
        // must only have one child of each placement

        BoardPosition existing = this.children.stream()
                .filter( c -> c.getPlacement().equals(placement) )
                .findFirst().orElse(null);

        if (existing != null) {
            // Really this shouldn't be happening: we shouldn't be trying to add a new placement when there's already one
            // But behave as best we can in this case.
            log.warn("addMove attempted to add an existing position.  Updating instead. " + placement);
            existing.setCategory(category, user_id);
            return existing;
        } else {
            // TBD possibly we should check if there is an inactive/deleted node and bring it back
            // instead of creating a new one in that case.
            BoardPosition child = new BoardPosition(this.play, placement, category, user_id);

            String previous_children = this.children.toString();

            children.add(child);
            child.setParent(this);
            child.variation_label = '_';
            log.info("Added move: " + placement);
            log.info("now this node: " + this.toString());
            this.audits.add(new Audit(this, ChangeType.ADD_CHILD, previous_children, child.getPlay(),"Added child " + placement, user_id));
            return child;
        }
    }

    public void setTags(List<Tag> tags) {
        log.info("Setting tags " + tags.toString());
        this.tags = new ArrayList<>();
        this.tags.addAll(tags);
    }

    public void setTag(Tag tag) {
        log.info("Setting tag " + tag.toString() );
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }
}
