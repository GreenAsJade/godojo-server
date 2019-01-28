package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SequenceDTO extends HalResource {
	
    private final String sequence;
    public String getSequence() {return sequence;}
    
    private final PlayCategory category;
    public PlayCategory getCategory() {return category;}

    @JsonCreator
    public SequenceDTO(
    		@JsonProperty("sequence") String sequence,
    		@JsonProperty("category") String category) {
    	this.sequence = sequence;
    	this.category = PlayCategory.valueOf(category.toUpperCase());
    }
    
    public SequenceDTO(BoardPosition move) {
        this.sequence = move.getPlacement();
        this.category = move.getCategory();
    }
}
