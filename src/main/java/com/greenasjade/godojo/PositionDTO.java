package com.greenasjade.godojo;

//import org.springframework.hateoas.ResourceSupport;

//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;

public class PositionDTO extends HalResource {

    private final String play;

    public PositionDTO(String play) {
        this.play = play;
    }

    public String getPlay() {
      return play;
    }
}
