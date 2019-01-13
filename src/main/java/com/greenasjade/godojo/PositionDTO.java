package com.greenasjade.godojo;

//import org.springframework.hateoas.ResourceSupport;

//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionDTO extends HalResource {

    private final String play;
    public String getPlay() {return play;}
    
    private final String description;
    public String getDescription() {return description;}

    public PositionDTO(BoardPosition position) {
        play = position.getPlay();
        description = position.getDescription();
    }
}
