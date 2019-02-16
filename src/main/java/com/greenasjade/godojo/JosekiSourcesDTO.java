package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class JosekiSourcesDTO {

    private List<JosekiSource> sources;

    // Outbound response
    public JosekiSourcesDTO(List<JosekiSource> sources) {
        this.sources = sources;
    }
}
