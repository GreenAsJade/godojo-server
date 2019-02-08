package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveDTO {
	
    private final String placement;
    public String getPlacement() {return placement;}
    
    private final PlayCategory category;
    public PlayCategory getCategory() {return category;}

    private Long node_id;
    public Long getNodeId() {return node_id;}

    @JsonCreator
    public MoveDTO(
    		@JsonProperty("placement") String placement,
    		@JsonProperty("category") String category) {
    	this.placement = placement;
    	this.category = PlayCategory.valueOf(category.toUpperCase());
    }
    
    public MoveDTO(BoardPosition move) {
        this.placement = move.getPlacement();
        this.category = move.getCategory();
        this.node_id = move.id;
    }
}
