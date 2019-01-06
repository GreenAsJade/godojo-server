/* This is DTO for the /position service */

package com.greenasjade.godojo;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Position extends ResourceSupport {

    private final String play;

    @JsonCreator
    public Position(@JsonProperty("play") String play) {
        this.play = play;
    }

    public String getPlay() {
      return play;
    }
}
