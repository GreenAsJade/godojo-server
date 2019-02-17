package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SequenceDTO {
	
    private final String sequence;
    
    private final PlayCategory category;

    public final Long joseki_source_id;

    @JsonCreator
    public SequenceDTO(
    		@JsonProperty("sequence") String sequence,
    		@JsonProperty("category") String category,
            @JsonProperty("josekisource") Long source) {
    	this.sequence = sequence;
    	this.category = PlayCategory.valueOf(category.toUpperCase());
    	this.joseki_source_id = source != null ? source : 0L;
    }
}
