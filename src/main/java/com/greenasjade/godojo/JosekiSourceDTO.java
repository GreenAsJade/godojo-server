package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Data;

import java.util.List;

@Data
public class JosekiSourceDTO {

    private JosekiSource source;

    @JsonCreator
    public JosekiSourceDTO(JosekiSource source) {
        this.source = source;
    }
}
