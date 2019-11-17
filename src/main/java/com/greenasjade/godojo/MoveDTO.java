package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MoveDTO {
	
    private final String placement;
    
    private final PlayCategory category;

    private final Character variation_label;

    private String node_id; // client wants these to be strings, compatible with "root" as the root id

    /* Not used?
    // Inbound info.
    @JsonCreator
    public MoveDTO(
    		@JsonProperty("placement") String placement,
    		@JsonProperty("category") String category) {
    	this.placement = placement;
    	this.category = PlayCategory.valueOf(category.toUpperCase());
    }
    */

    // Outbound move information
    // This DTO is intended to carry the variations of a position.
    // Possibly should be called "VariationDTO"?
    public MoveDTO(BoardPosition move) {
        this.placement = move.getPlacement();
        this.category = move.getCategory();
        this.node_id = move.id.toString();
        this.variation_label = move.getVariationLabel();
    }
}
