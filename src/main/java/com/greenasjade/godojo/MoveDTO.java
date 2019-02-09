package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MoveDTO {
	
    private final String placement;
    
    private final PlayCategory category;

    private Long node_id;

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
