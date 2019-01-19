package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionDTO extends HalResource {

    private final String play;
    public String getPlay() {return play;}

    private String description;
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

    private final String title;
    public String getTitle() {return title;}

    @JsonCreator
    public PositionDTO(@JsonProperty("description") String description) {
        this.description = description;
        this.title="";
        this.play="";
    }
    public PositionDTO(BoardPosition position) {
        play = position.getPlay();
        title = position.getTitle();
        description = position.getDescription();
    }
}
