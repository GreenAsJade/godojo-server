package com.greenasjade.godojo;

import java.util.*;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public abstract class HalResource extends ResourceSupport {

    private final Map<String, Object> embedded = new HashMap<String, Object>();

    @JsonInclude(Include.NON_EMPTY)
    @JsonProperty("_embedded")
    public Map<String, Object> getEmbeddedResources() {
        return embedded;
    }

    public void embed(String relationship, Object resource) {
        embedded.put(relationship, resource);
    }  
}
