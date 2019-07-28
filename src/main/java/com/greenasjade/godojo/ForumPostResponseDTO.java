package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

// This is a DTO for the Discourse post response

// We actually only care about the topic_id that we get back

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumPostResponseDTO {

    @JsonProperty("topic_id")
    private Integer topicId;

    public ForumPostResponseDTO(){}
}
