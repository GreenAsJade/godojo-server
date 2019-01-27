package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SequenceDTO extends HalResource {
	
    private final String sequence;
    public String getSequence() {return sequence;}
    
    private final MoveCategory category;
    public MoveCategory getCategory() {return category;}

    @JsonCreator
    public SequenceDTO(
    		@JsonProperty("sequence") String sequence,
    		@JsonProperty("category") String category) {
    	this.sequence = sequence;
    	this.category = MoveCategory.valueOf(category.toUpperCase());
    }
    
    public SequenceDTO(Move move) {
        this.sequence = move.getPlacement();
        this.category = move.getCategory();
    }
}
