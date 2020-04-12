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
    public void setVariationLabel(Character label) {
        if ("123456789_".indexOf(label) >= 0) {
            variation_label = label;
        } else {
            log.warn("Asked to set an invalid variation label, set a valid one instead!");
            variation_label = this.nextVariationLabel();
        }
    }

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

    @Property("forum_thread_id")
    private Integer forum_thread_id;
    public Integer getForumThreadId(){return forum_thread_id;}
    public void setForumThreadId(Integer id) {forum_thread_id = id;}

    @Property("marks")
    private String marks;  // note that this is an opaque string as far as server is concerned
    public String getMarks() {return marks;}
    public void setMarks(String marks){this.marks = marks;}

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
        this.variation_label = '_';

        this.children = new ArrayList<>();
        this.commentary = new ArrayList<>();
        this.tags = new ArrayList<>();

        //log.debug(placement + " created by user " + user_id.toString());

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

        // The only way to uniquely identify this comment is it's date (it doesn't have an ID yet)
        this.audits.add(new Audit(this, ChangeType.ADD_COMMENT, new_comment.getDate().toString(), text, "Commented", user_id));
    }

    public String toString() {
        return this.play;
    }

    public String getInfo() {
        String p = this.parent==null ? "." : this.parent.getPlacement();

        String i = this.id==null ? "tbd" : this.id.toString();

        String u = this.contributor_id.toString();

        String child_list =
                Optional.ofNullable(this.children).orElse(Collections.emptyList()).stream()
                        .map(BoardPosition::getPlacement)
                        .collect(Collectors.toList()).toString();

        return p + " -> " +  "<"+i+">" + "("+u+")"+ this.play +
                " -> " + child_list;
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

            child.variation_label = this.nextVariationLabel();
            children.add(child);
            child.setParent(this);

            //log.debug("Added move: " + placement);
            //log.debug("now this node: " + this.toString());
            this.audits.add(new Audit(this, ChangeType.ADD_CHILD, previous_children, child.getPlay(),"Added child " + placement, user_id));
            return child;
        }
    }

    Character nextVariationLabel() {
        if (this.children == null) {
            log.warn("Asked to get next variation label of node with null children - unexpected");
            return '1';
        }

        List<BoardPosition> labelled = this.children.stream()
                .filter(p -> p.variation_label != null && p.variation_label != '_' && !p.placement.equals("pass"))
                .collect(Collectors.toList());

        if (labelled.size() < 9) {
            return (char)(labelled.size() + '1');
        }
        else {
            return '_';
        }
    }

    public void setTags(List<Tag> tags) {
        J01Application.debug("Setting tags " + tags.toString(), log);
        this.tags = new ArrayList<>();
        this.tags.addAll(tags);
    }

    public void setTag(Tag tag) {
        //log.debug("Setting tag " + tag.toString() );
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tag);
    }
}
