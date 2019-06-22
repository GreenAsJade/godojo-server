package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SequenceDTO {
	
    private final String sequence;

    private final PlayCategory category;

    @JsonCreator
    public SequenceDTO(
    		@JsonProperty("sequence") String sequence,
    		@JsonProperty("category") String category) {
    	this.sequence = sequence;
    	this.category = PlayCategory.valueOf(category.toUpperCase());
    }
}
