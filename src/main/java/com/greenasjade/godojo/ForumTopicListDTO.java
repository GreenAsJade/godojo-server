package com.greenasjade.godojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.Instant;
import java.util.List;

// This is a DTO for the Discourse topic_list https://docs.discourse.org/#tag/Categories%2Fpaths%2F~1categories.json%2Fpost

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForumTopicListDTO {

    private List<ForumTopicDTO> topics;

    public ForumTopicListDTO(){}
}
