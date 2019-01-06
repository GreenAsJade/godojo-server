/* This is DTO for the /position service */

package com.greenasjade.godojo;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Position extends ResourceSupport {

    private final String content;

    @JsonCreator
    public Position(@JsonProperty("content") String content) {
        this.content = content;
    }

    public String getContent() {
      return content;
    }
}
