package com.greenasjade.godojo;

//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveDTO extends HalResource {

    private final String placement;
    public String getPlacement() {return placement;}
    
    private final MoveCategory category;
    public MoveCategory getCategory() {return category;}

    public MoveDTO(Move move) {
        this.placement = move.getPlacement();
        this.category = move.getCategory();
    }
}
