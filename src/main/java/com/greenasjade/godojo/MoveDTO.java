package com.greenasjade.godojo;

//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonProperty;

public class MoveDTO extends HalResource {

    private final String placement;

    public MoveDTO(String placement) {
        this.placement = placement;
    }

    public String getPlacement() {
      return placement;
    }
}
