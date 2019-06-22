package com.greenasjade.godojo;

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
