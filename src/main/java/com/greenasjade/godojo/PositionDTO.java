package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionDTO extends HalResource {

    private String description;
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    private PlayCategory move_type;
    public PlayCategory getMoveType() {return move_type;}

    @JsonCreator
    public PositionDTO(
            @JsonProperty("description") String description,
            @JsonProperty("move_type") String move_type) {
        this.description = description;
        // empty move_type means "don't change it"
        this.move_type = move_type.equals("") ? null : PlayCategory.valueOf(move_type);
    }

    public PositionDTO(BoardPosition position) {
        description = position.getDescription();
        move_type = position.getCategory();
    }
}
