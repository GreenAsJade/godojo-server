package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Data
public class JosekiSourceDTO {

    private static final Logger log = LoggerFactory.getLogger(JosekiSourceDTO.class);

    private JosekiSource source;

    @JsonCreator
    public JosekiSourceDTO(@JsonProperty("source") JosekiSource source) {
        this.source = source;
        //log.debug("Creating Source DTO with description: " + source.getDescription());
    }
}
