package com.greenasjade.godojo;

public class PositionDTO extends HalResource {

    private final String play;
    public String getPlay() {return play;}
    
    private final String description;
    public String getDescription() {return description;}

    private final String title;
    public String getTitle() {return title;}
    
    public PositionDTO(BoardPosition position) {
        play = position.getPlay();
        title = position.getTitle();
        description = position.getDescription();
    }
}
