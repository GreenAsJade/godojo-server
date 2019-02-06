package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionDTO extends HalResource {

    private String description;
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    private PlayCategory category;
    public PlayCategory getCategory() {return category;}

    private String placement;
    public String getPlacement() {return placement;}

    private String play;
    public String getPlay() {return play;}

    private Integer contributor;
    public Integer getContributor() {return contributor;}

    private Long node_id;
    public Long getNodeId() {return node_id;}

    private Integer comment_count;
    public Integer getCommentCount() {return comment_count;}

    // Inbound position information
    @JsonCreator
    public PositionDTO(
            @JsonProperty("description") String description,
            @JsonProperty("category") String category) {
        this.description = description;
        // empty move_type means "don't change it"
        this.category = category.equals("") ? null : PlayCategory.valueOf(category);
    }

    // Outbound position information
    public PositionDTO(BoardPosition position) {
        description = position.getDescription();
        category = position.getCategory();
        placement = position.getPlacement();
        contributor = position.getContributorId();
        play = position.getPlay();
        node_id = position.id;
        comment_count = position.getCommentCount();
    }
}
